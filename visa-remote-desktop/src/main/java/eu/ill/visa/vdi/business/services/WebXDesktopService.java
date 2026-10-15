package eu.ill.visa.vdi.business.services;

import eu.ill.visa.common.vdi.apiconnect.VisaApiService;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.webx.WebXClientConfiguration;
import eu.ill.webx.WebXEngineConfiguration;
import eu.ill.webx.WebXHostConfiguration;
import eu.ill.webx.WebXTunnel;
import eu.ill.webx.exceptions.WebXClientException;
import eu.ill.webx.exceptions.WebXConnectionException;
import eu.ill.webx.exceptions.WebXException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SignatureException;
import java.util.Map;

import static java.util.Map.entry;

@ApplicationScoped
public class WebXDesktopService extends DesktopService {

    private static final Logger logger = LoggerFactory.getLogger(WebXDesktopService.class);
    private static final String WEBX_CLIENT_VERSION = "client-version";

    private final static Map<String, String> KeyboardLayoutConversion = Map.ofEntries(
        entry("en-gb-qwerty", "gb"),
        entry("en-us-qwerty", "us"),
        entry("fr-fr-azerty", "fr"),
        entry("de-de-qwertz", "de"),
        entry("fr-be-azerty", "be"),
        entry("fr-ch-qwertz", "ch(fr)"),
        entry("hu-hu-qwertz", "hu"),
        entry("it-it-qwerty", "it"),
        entry("ja-jp-qwerty", "jp"),
        entry("pt-br-qwerty", "br"),
        entry("es-es-qwerty", "es"),
        entry("es-latam-qwerty", "latam"),
        entry("sv-se-qwerty", "se"),
        entry("tr-tr-qwerty", "tr"),
        entry("da-dk-qwerty", "dk"),
        entry("de-ch-qwertz", "ch(de)"),
        entry("no-no-qwerty", "no")
    );

    private final VisaApiService visaApiService;
    private final ConnectionThreadExecutor executorService;
    private final VirtualDesktopConfiguration configuration;

    @Inject
    public WebXDesktopService(final jakarta.enterprise.inject.Instance<VisaApiService> visaApiServiceInstance,
                              final ConnectionThreadExecutor executorService,
                              final VirtualDesktopConfiguration configuration) {
        this.visaApiService = visaApiServiceInstance.get();
        this.executorService = executorService;
        this.configuration = configuration;
    }

    private WebXClientConfiguration createClientConfiguration(final SocketClient client,
                                                              final VdiInstance instance,
                                                              final String connectionId) throws ConnectionException {
        try {
            String clientVersion = client.getStringRequestParameter(WEBX_CLIENT_VERSION).orElse("0.0.0");

            if (connectionId == null) {
                final Integer screenHeight = instance.screenHeight();
                final Integer screenWidth = instance.screenWidth();
                String username = instance.username();
                logger.info("Creating new WebX session on instance {} with username {}", instance.id(), username);
                String autologin = instance.autoLogin();
                String password = null;
                if (autologin != null && autologin.equals("VISA_PAM")) {
                    password = this.visaApiService.createVdiConnectionSignature(username);
                }
                String keyboardLayout = KeyboardLayoutConversion.getOrDefault(instance.keyboardLayout(), instance.keyboardLayout());

                return WebXClientConfiguration.ForLogin(username, password, screenWidth, screenHeight, keyboardLayout, clientVersion);

            } else {
                // use session Id to connect to remote desktop
                logger.info("Connecting to existing WebX session on instance {} with session Id {}", instance.id(), connectionId);
                return WebXClientConfiguration.ForExistingSession(connectionId, clientVersion);
            }

        } catch (SignatureException e) {
            throw new ConnectionException(String.format("Failed to obtain a signature for the webx session on instance %d with username %s", instance.id(), instance.username()));
        }
    }

    private WebXEngineConfiguration createEngineConfiguration() {
        final WebXEngineConfiguration config = new WebXEngineConfiguration();
        final Map<String, String> webxConfiguration = configuration.webxConfiguration();
        webxConfiguration.forEach(config::setParameter);
        return config;
    }

    private WebXHostConfiguration createHostConfiguration(final VdiInstance instance) {
        Integer port = instance.port() != null ? instance.port() : 5555;
        String hostname = instance.ipAddress();

        return new WebXHostConfiguration(hostname, port);
    }

    private WebXTunnel buildTunnel(final SocketClient client,
                                   final VdiInstance instance) throws WebXConnectionException, ConnectionException {
        return this.buildTunnel(client, instance, null);
    }

    private WebXTunnel buildTunnel(final SocketClient client,
                                   final VdiInstance instance,
                                   final String connectionId) throws WebXConnectionException, ConnectionException {

        final WebXHostConfiguration hostConfiguration = createHostConfiguration(instance);
        final WebXClientConfiguration clientConfiguration = createClientConfiguration(client, instance, connectionId);
        final WebXEngineConfiguration engineConfiguration = createEngineConfiguration();

        WebXTunnel tunnel = new WebXTunnel();
        tunnel.connect(hostConfiguration, clientConfiguration, engineConfiguration);

        return tunnel;
    }

    private WebXTunnel createTunnelAndSession(final SocketClient client,
                                              final VdiInstance instance,
                                              final ConnectedUser user) throws OwnerNotConnectedException, WebXConnectionException, WebXClientException, ConnectionException {
        // Create new session if user is owner
        if (user.role().equals(InstanceMemberRole.OWNER) || instance.canConnectWhileOwnerAway()) {
            final WebXTunnel tunnel = this.buildTunnel(client, instance);
            logger.info("{} created WebX session with Id {}", getInstanceAndUser(instance, user), tunnel.getConnectionId());

            return tunnel;

        } else {
            logger.warn("A non-owner - {} - is trying to create a new instance session", getInstanceAndUser(instance, user));
            throw new OwnerNotConnectedException();
        }
    }

    private WebXTunnel getOrCreateTunnel(final SocketClient client,
                                         final VdiInstance instance,
                                         final ConnectedUser user,
                                         final String connectionId) throws OwnerNotConnectedException, WebXConnectionException, WebXClientException, ConnectionException {

        if (connectionId == null) {
            return this.createTunnelAndSession(client, instance, user);

        } else {
            try {
                // try to connect to existing sessionId
                logger.info("User {} connecting to existing WebX session {}", getInstanceAndUser(instance, user), connectionId);
                return buildTunnel(client, instance, connectionId);

            } catch (WebXConnectionException exception) {
                logger.warn("Failed to connect {} to given WebX session {} so creating a new one", getInstanceAndUser(instance, user), connectionId);
                // If it fails then invalidate current session
                try {
                    this.visaApiService.onSessionConnectionFailed(instance.id(), WEBX_PROTOCOL, connectionId, client.clientId(), user);

                } catch (ApiCommunicationException e) {
                    logger.warn(e.getMessage());
                }

                // Create a new session
                return this.createTunnelAndSession(client, instance, user);
            }
        }
    }
    private WebXTunnel createWebXTunnel(final SocketClient client,
                                        final VdiInstance instance,
                                        final ConnectedUser user,
                                        final String connectionId) throws OwnerNotConnectedException, ConnectionException {
        try {
            return getOrCreateTunnel(client, instance, user, connectionId);

        } catch (WebXException exception) {
            throw new ConnectionException("Error connecting to tunnel for " + this.getInstanceAndUser(instance, user) + " : " + exception.getMessage());
        }
    }

    @Override
    public RemoteDesktopConnection connect(SocketClient client, VdiInstance instance, ConnectedUser user, String connectionId) throws OwnerNotConnectedException, ConnectionException {
        final WebXTunnel webXTunnel = this.createWebXTunnel(client, instance, user, connectionId);
        final ConnectionThread connectionThread = executorService.createWebXConnectionThread(client, webXTunnel, instance, user);
        try {
            return new RemoteDesktopConnection(client, connectionThread, webXTunnel.getConnectionId());

        } catch (WebXClientException e) {
            throw new ConnectionException(String.format("Cannot get client connection id: %s", e.getMessage()));
        }

    }
}
