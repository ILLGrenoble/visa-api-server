package eu.ill.visa.vdi.business.services;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.Map;

@ApplicationScoped
public class GuacamoleDesktopService extends DesktopService {

    private final static Logger logger = LoggerFactory.getLogger(GuacamoleDesktopService.class);

    private final VisaApiService visaApiService;
    private final VirtualDesktopConfiguration configuration;
    private final ConnectionThreadExecutor executorService;

    @Inject
    public GuacamoleDesktopService(final jakarta.enterprise.inject.Instance<VisaApiService> visaApiServiceInstance,
                                   final VirtualDesktopConfiguration configuration,
                                   final ConnectionThreadExecutor executorService) {
        this.visaApiService = visaApiServiceInstance.get();
        this.configuration = configuration;
        this.executorService = executorService;
    }

    private GuacamoleClientInformation createClientInformation(final VdiInstance instance) {
        final Integer height = instance.screenHeight();
        final Integer width = instance.screenWidth();
        final GuacamoleClientInformation info = new GuacamoleClientInformation();
        info.setOptimalScreenHeight(height);
        info.setOptimalScreenWidth(width);
        info.getImageMimetypes().add("image/png");
        return info;
    }

    private GuacamoleConfiguration createConfiguration(final String connectionId, final VdiInstance instance, final String ip) throws ConnectionException {
        try {
            final GuacamoleConfiguration config = new GuacamoleConfiguration();
            config.setParameter("hostname", ip);

            if (connectionId == null) {
                // Get the protocol from the instance image or use the default configuration
                final String secondaryVdiProtocol = instance.secondaryVdiProtocol();
                if (secondaryVdiProtocol != null && Arrays.asList("RDP", "VNC").contains(secondaryVdiProtocol)) {
                    config.setProtocol(secondaryVdiProtocol.toLowerCase());

                } else {
                    // Fallback to the default protocol defined in the configuration
                    config.setProtocol(configuration.protocol());
                }

                String username = instance.username();
                logger.info("Creating new guacamole session on instance {} with username {}", instance.id(), username);
                String autologin = instance.autoLogin();
                config.setParameter("username", username);
                if (autologin != null && autologin.equals("VISA_PAM")) {
                    config.setParameter("password", this.visaApiService.createVdiConnectionSignature(username));
                }
                config.setParameter("server-layout", instance.keyboardLayout());
                // merge guacamole configuration parameters
                final Map<String, String> guacamoleParameters = configuration.guacdConfiguration();
                guacamoleParameters.forEach(config::setParameter);
            } else {
                config.setConnectionID(connectionId);
            }
            return config;

        } catch (SignatureException e) {
            throw new ConnectionException(String.format("Failed to obtain a signature for the guacamole session on instance %d with username %s", instance.id(), instance.username()));
        }
    }

    private ConfiguredGuacamoleSocket buildSocket(final VdiInstance instance) throws GuacamoleException, ConnectionException {
        return this.buildSocket(instance, null);
    }

    private ConfiguredGuacamoleSocket buildSocket(final VdiInstance instance,
                                                  final String connectionId) throws GuacamoleException, ConnectionException {
        int port = instance.port() != null ? instance.port() : 4822;
        final String ip = instance.ipAddress();
        final GuacamoleConfiguration config = createConfiguration(connectionId, instance, ip);
        final GuacamoleClientInformation information = createClientInformation(instance);
        final InetGuacamoleSocket socket = new InetGuacamoleSocket(ip, port);
        return new ConfiguredGuacamoleSocket(socket, config, information);
    }

    private ConfiguredGuacamoleSocket createSocketAndSession(final VdiInstance instance, final ConnectedUser user) throws OwnerNotConnectedException, GuacamoleException, ConnectionException {
        // Create new session if user is owner
        if (user.role().equals(InstanceMemberRole.OWNER) || instance.canConnectWhileOwnerAway()) {
            final ConfiguredGuacamoleSocket socket = buildSocket(instance);
            logger.info("User {} created guacamole session {}", getInstanceAndUser(instance, user), socket.getConnectionID());

            return socket;

        } else {
            logger.warn("A non-owner - {} - is trying to create a new instance session", getInstanceAndUser(instance, user));
            throw new OwnerNotConnectedException();
        }
    }

    private ConfiguredGuacamoleSocket getOrCreateSocket(final SocketClient client, final VdiInstance instance, final ConnectedUser user, final String connectionId) throws OwnerNotConnectedException, GuacamoleException, ConnectionException {
        if (connectionId == null) {
            return this.createSocketAndSession(instance, user);

        } else {
            try {
                // try to connect to existing sessionId
                logger.info("{} connecting to existing guacamole session {}", getInstanceAndUser(instance, user), connectionId);
                return buildSocket(instance, connectionId);

            } catch (GuacamoleException exception) {
                logger.warn("Failed to connect {} to given guacamole session {} so creating a new one", getInstanceAndUser(instance, user), connectionId);
                // If it fails then invalidate current session
                try {
                    this.visaApiService.onSessionConnectionFailed(instance.id(), GUACAMOLE_PROTOCOL, connectionId, client.clientId(), user);

                } catch (ApiCommunicationException e) {
                    logger.warn(e.getMessage());
                }

                // Create a new session
                return this.createSocketAndSession(instance, user);
            }
        }
    }

    private ConfiguredGuacamoleSocket createGuacamoleSocket(final SocketClient client, final VdiInstance instance, final ConnectedUser user, final String connectionId) throws OwnerNotConnectedException, ConnectionException {
        try {
            synchronized (instance) {
                final ConfiguredGuacamoleSocket socket = getOrCreateSocket(client, instance, user, connectionId);

                return socket;
            }

        } catch (GuacamoleException exception) {
            throw new ConnectionException("Error connecting to tunnel for " + this.getInstanceAndUser(instance, user) + " : " + exception.getMessage());
        }
    }

    @Override
    public RemoteDesktopConnection connect(final SocketClient client,
                                           final VdiInstance instance,
                                           final ConnectedUser user,
                                           final String connectionId) throws OwnerNotConnectedException, ConnectionException {
        final ConfiguredGuacamoleSocket guacamoleSocket = this.createGuacamoleSocket(client, instance, user, connectionId);

        final GuacamoleTunnel guacamoleTunnel = new SimpleGuacamoleTunnel(guacamoleSocket);
        final ConnectionThread connectionThread = executorService.createGuacamoleConnectionThread(client, guacamoleTunnel, instance, user);
        return new RemoteDesktopConnection(client, connectionThread, guacamoleSocket.getConnectionID());
    }
}
