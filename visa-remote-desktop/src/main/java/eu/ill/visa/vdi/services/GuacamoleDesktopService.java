package eu.ill.visa.vdi.services;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.concurrency.ConnectionThread;
import eu.ill.visa.vdi.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.exceptions.ConnectionException;
import eu.ill.visa.vdi.exceptions.OwnerNotConnectedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static eu.ill.visa.vdi.domain.Role.OWNER;
import static java.util.Objects.requireNonNullElse;

public class GuacamoleDesktopService extends DesktopService {

    private final static Logger logger = LoggerFactory.getLogger(GuacamoleDesktopService.class);

    private final InstanceSessionService instanceSessionService;
    private final SignatureService signatureService;
    private final ImageProtocolService imageProtocolService;
    private final VirtualDesktopConfiguration configuration;
    private final ConnectionThreadExecutor executorService;

    public GuacamoleDesktopService(final InstanceSessionService instanceSessionService,
                            final CloudClientService cloudClientService,
                            final SignatureService signatureService,
                            final ImageProtocolService imageProtocolService,
                            final VirtualDesktopConfiguration configuration,
                            final ConnectionThreadExecutor executorService) {
        super(cloudClientService);

        this.instanceSessionService = instanceSessionService;
        this.signatureService = signatureService;
        this.imageProtocolService = imageProtocolService;
        this.configuration = configuration;
        this.executorService = executorService;
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
        config.setProtocol(configuration.protocol());
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
            final Map<String, String> guacamoleParameters = configuration.guacdConfiguration();
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

    private GuacamoleSocket createGuacamoleSocket(final Instance instance,
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

    @Override
    public ConnectionThread connect(final SocketIOClient client,
                                    final Instance instance,
                                    final User user,
                                    final Role role) throws OwnerNotConnectedException, ConnectionException {
        final GuacamoleSocket guacamoleSocket = this.createGuacamoleSocket(instance, user, role);

        final GuacamoleTunnel guacamoleTunnel = new SimpleGuacamoleTunnel(guacamoleSocket);
        return executorService.startGuacamoleConnectionThread(client, guacamoleTunnel, instance, user, role);
    }
}
