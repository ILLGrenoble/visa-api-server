package eu.ill.visa.vdi.services;

import com.google.inject.Inject;
import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.ImageProtocol;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSession;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.exceptions.ConnectionException;
import eu.ill.visa.vdi.exceptions.OwnerNotConnectedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static eu.ill.visa.vdi.domain.Role.OWNER;
import static java.util.Objects.requireNonNullElse;

public class GuacamoleSocketService {

    private final static Logger                      logger = LoggerFactory.getLogger(GuacamoleSocketService.class);
    private final        InstanceSessionService      instanceSessionService;
    private final CloudClientGateway cloudClientGateway;
    private final        SignatureService            signatureService;
    private final        ImageProtocolService        imageProtocolService;
    private final        VirtualDesktopConfiguration configuration;

    @Inject
    GuacamoleSocketService(final InstanceSessionService instanceSessionService,
                           final CloudClientGateway cloudClientGateway,
                           final SignatureService signatureService,
                           final ImageProtocolService imageProtocolService,
                           final VirtualDesktopConfiguration configuration) {
        this.instanceSessionService = instanceSessionService;
        this.cloudClientGateway = cloudClientGateway;
        this.signatureService = signatureService;
        this.imageProtocolService = imageProtocolService;
        this.configuration = configuration;
    }

    private GuacamoleClientInformation createClientInformation(final Instance instance) {
        final Integer height = instance.getScreenHeight();
        final Integer width = instance.getScreenWidth();
        final GuacamoleClientInformation info = new GuacamoleClientInformation();
        info.setOptimalScreenHeight(height);
        info.setOptimalScreenWidth(width);
        info.getImageMimetypes().add("image/png");
        return info;
    }

    private GuacamoleConfiguration createConfiguration(InstanceSession session, Instance instance, String ip) {
        final GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setParameter("hostname", ip);
        config.setProtocol(configuration.getProtocol());
        if (session == null) {
            String username = instance.getUsername();
            logger.info("Creating new guacamole session on instance {} with username {}", instance.getId(), username);
            String autologin = instance.getPlan().getImage().getAutologin();
            config.setParameter("username", username);
            if (autologin != null && autologin.equals("VISA_PAM")) {
                config.setParameter("password", signatureService.createSignature(username));
            }
            config.setParameter("server-layout", instance.getKeyboardLayout());
            // merge guacamole configuration parameters
            final Map<String, String> guacamoleParameters = configuration.getGuacdConfiguration();
            guacamoleParameters.forEach(config::setParameter);
        } else {
            config.setConnectionID(session.getConnectionId());
        }
        return config;
    }

    private ConfiguredGuacamoleSocket buildSocket(final Instance instance) throws GuacamoleException, CloudException {
        return this.buildSocket(instance, null);
    }

    private ConfiguredGuacamoleSocket buildSocket(final Instance instance,
                                                  final InstanceSession session) throws GuacamoleException, CloudException {
        final ImageProtocol protocol = requireNonNullElse(
            imageProtocolService.getByName("GUACD"),
            new ImageProtocol("GUACD", 4822)
        );
        final Integer port = protocol.getPort();
        final String ip = getIpAddressForInstance(instance);
        final GuacamoleConfiguration config = createConfiguration(session, instance, ip);
        final GuacamoleClientInformation information = createClientInformation(instance);
        final InetGuacamoleSocket socket = new InetGuacamoleSocket(ip, port);
        return new ConfiguredGuacamoleSocket(socket, config, information);
    }

    private String getIpAddressForInstance(Instance instance) throws CloudException {
        if (instance.getIpAddress() == null) {
            // TODO CloudClient: select specific cloud client
            CloudClient cloudClient = this.cloudClientGateway.getDefaultCloudClient();
            return cloudClient.ip(instance.getComputeId());
        }
        return instance.getIpAddress();
    }

    private ConfiguredGuacamoleSocket createSocketAndSession(Instance instance, User user, Role role) throws OwnerNotConnectedException, GuacamoleException, CloudException {
        // Create new session if user is owner
        if (role.equals(OWNER) || instanceSessionService.canConnectWhileOwnerAway(instance, user)) {
            final ConfiguredGuacamoleSocket socket = buildSocket(instance);
            InstanceSession session = instanceSessionService.create(instance, socket.getConnectionID());
            logger.info("User {} created guacamole session {}", getInstanceAndUser(instance, user, role), session.getConnectionId());

            return socket;

        } else {
            logger.warn("A non-owner - {} - is trying to create a new instance session", getInstanceAndUser(instance, user, role));
            throw new OwnerNotConnectedException();
        }
    }

    private ConfiguredGuacamoleSocket getOrCreateSocket(Instance instance, User user, Role role) throws OwnerNotConnectedException, GuacamoleException, CloudException {
        InstanceSession session = instanceSessionService.getByInstance(instance);

        if (session == null) {
            return this.createSocketAndSession(instance, user, role);

        } else {
            try {
                // try to connect to existing sessionId
                logger.info("User {} connecting to existing guacamole session {}", getInstanceAndUser(instance, user, role), session.getConnectionId());
                return buildSocket(instance, session);

            } catch (GuacamoleException exception) {
                logger.error("Failed to connect {} to given guacamole session {} so creating a new one", getInstanceAndUser(instance, user, role), session.getConnectionId());
                // If it fails then invalidate current session
                session.setCurrent(false);
                this.instanceSessionService.save(session);

                // Create a new session
                return this.createSocketAndSession(instance, user, role);
            }
        }
    }

    public GuacamoleSocket createGuacamoleSocket(final Instance instance,
                                                              final User user,
                                                              final Role role) throws OwnerNotConnectedException, ConnectionException {
        try {
            synchronized (instance) {
                final GuacamoleSocket socket = getOrCreateSocket(instance, user, role);

                return socket;
            }

        } catch (GuacamoleException exception) {
            throw new ConnectionException("Error connecting to tunnel for " + this.getInstanceAndUser(instance, user, role) + " : " + exception.getMessage());
        } catch (CloudException exception) {
            throw new ConnectionException("There was an exception contacting the cloud for : " + this.getInstanceAndUser(instance, user, role) + " : " + exception.getMessage());
        }
    }

    private String getInstanceAndUser(Instance instance, User user, Role role) {
        return "User " + user.getFullName() + " (" + user.getId() + ", " + role.toString() + "), Instance " + instance.getId();
    }

}
