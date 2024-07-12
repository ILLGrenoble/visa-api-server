package eu.ill.visa.vdi.business.services;

import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

import static java.util.Objects.requireNonNullElse;

@ApplicationScoped
public class GuacamoleDesktopService extends DesktopService {

    private final static Logger logger = LoggerFactory.getLogger(GuacamoleDesktopService.class);

    private final InstanceSessionService instanceSessionService;
    private final SignatureService signatureService;
    private final ImageProtocolService imageProtocolService;
    private final VirtualDesktopConfiguration configuration;
    private final ConnectionThreadExecutor executorService;

    @Inject
    public GuacamoleDesktopService(final InstanceSessionService instanceSessionService,
                            final SignatureService signatureService,
                            final ImageProtocolService imageProtocolService,
                            final VirtualDesktopConfiguration configuration,
                            final ConnectionThreadExecutor executorService) {
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

    private ConfiguredGuacamoleSocket buildSocket(final Instance instance) throws GuacamoleException {
        return this.buildSocket(instance, null);
    }

    private ConfiguredGuacamoleSocket buildSocket(final Instance instance,
                                                  final InstanceSession session) throws GuacamoleException {
        final ImageProtocol protocol = requireNonNullElse(
            imageProtocolService.getByName("GUACD"),
            new ImageProtocol("GUACD", 4822)
        );
        final Integer port = protocol.getPort();
        final String ip = instance.getIpAddress();
        final GuacamoleConfiguration config = createConfiguration(session, instance, ip);
        final GuacamoleClientInformation information = createClientInformation(instance);
        final InetGuacamoleSocket socket = new InetGuacamoleSocket(ip, port);
        return new ConfiguredGuacamoleSocket(socket, config, information);
    }

    private ConfiguredGuacamoleSocket createSocketAndSession(Instance instance, ConnectedUser user) throws OwnerNotConnectedException, GuacamoleException {
        // Create new session if user is owner
        if (user.getRole().equals(InstanceMemberRole.OWNER) || instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
            final ConfiguredGuacamoleSocket socket = buildSocket(instance);
            InstanceSession session = instanceSessionService.create(instance, GUACAMOLE_PROTOCOL, socket.getConnectionID());
            logger.info("User {} created guacamole session {}", getInstanceAndUser(instance, user), session.getConnectionId());

            return socket;

        } else {
            logger.warn("A non-owner - {} - is trying to create a new instance session", getInstanceAndUser(instance, user));
            throw new OwnerNotConnectedException();
        }
    }

    private ConfiguredGuacamoleSocket getOrCreateSocket(Instance instance, ConnectedUser user) throws OwnerNotConnectedException, GuacamoleException {
        InstanceSession session = instanceSessionService.getByInstanceAndProtocol(instance, GUACAMOLE_PROTOCOL);

        if (session == null) {
            return this.createSocketAndSession(instance, user);

        } else {
            try {
                // try to connect to existing sessionId
                logger.info("User {} connecting to existing guacamole session {}", getInstanceAndUser(instance, user), session.getConnectionId());
                return buildSocket(instance, session);

            } catch (GuacamoleException exception) {
                logger.error("Failed to connect {} to given guacamole session {} so creating a new one", getInstanceAndUser(instance, user), session.getConnectionId());
                // If it fails then invalidate current session
                session.setCurrent(false);
                this.instanceSessionService.save(session);

                // Create a new session
                return this.createSocketAndSession(instance, user);
            }
        }
    }

    private GuacamoleSocket createGuacamoleSocket(final Instance instance, final ConnectedUser user) throws OwnerNotConnectedException, ConnectionException {
        try {
            synchronized (instance) {
                final GuacamoleSocket socket = getOrCreateSocket(instance, user);

                return socket;
            }

        } catch (GuacamoleException exception) {
            throw new ConnectionException("Error connecting to tunnel for " + this.getInstanceAndUser(instance, user) + " : " + exception.getMessage());
        }
    }

    @Override
    public RemoteDesktopConnection connect(final SocketClient client,
                                           final Instance instance,
                                           final ConnectedUser user) throws OwnerNotConnectedException, ConnectionException {
        final GuacamoleSocket guacamoleSocket = this.createGuacamoleSocket(instance, user);

        final GuacamoleTunnel guacamoleTunnel = new SimpleGuacamoleTunnel(guacamoleSocket);
        final ConnectionThread connectionThread = executorService.startGuacamoleConnectionThread(client, guacamoleTunnel, instance, user);
        return new RemoteDesktopConnection(client, connectionThread);
    }
}
