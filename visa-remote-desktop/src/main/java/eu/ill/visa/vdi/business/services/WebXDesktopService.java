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

import java.util.Map;

import static java.util.Objects.requireNonNullElse;
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

    private final InstanceSessionService instanceSessionService;
    private final SignatureService signatureService;
    private final ImageProtocolService imageProtocolService;
    private final ConnectionThreadExecutor executorService;
    private final VirtualDesktopConfiguration configuration;

    @Inject
    public WebXDesktopService(final InstanceSessionService instanceSessionService,
                              final SignatureService signatureService,
                              final ImageProtocolService imageProtocolService,
                              final ConnectionThreadExecutor executorService,
                              final VirtualDesktopConfiguration configuration) {
        this.instanceSessionService = instanceSessionService;
        this.signatureService = signatureService;
        this.imageProtocolService = imageProtocolService;
        this.executorService = executorService;
        this.configuration = configuration;
    }

    private WebXClientConfiguration createClientConfiguration(final SocketClient client, final Instance instance, final InstanceSession session) {
        String clientVersion = client.getStringRequestParameter(WEBX_CLIENT_VERSION).orElse("0.0.0");

        if (session == null) {
            final Integer screenHeight = instance.getScreenHeight();
            final Integer screenWidth = instance.getScreenWidth();
            String username = instance.getUsername();
            logger.info("Creating new WebX session on instance {} with username {}", instance.getId(), username);
            String autologin = instance.getPlan().getImage().getAutologin();
            String password = null;
            if (autologin != null && autologin.equals("VISA_PAM")) {
                password =  signatureService.createSignature(username);
            }
            String keyboardLayout = KeyboardLayoutConversion.getOrDefault(instance.getKeyboardLayout(), instance.getKeyboardLayout());

            return WebXClientConfiguration.ForLogin(username, password, screenWidth, screenHeight, keyboardLayout, clientVersion);

        } else {
            // use session Id to connect to remote desktop
            logger.info("Connecting to existing WebX session on instance {} with session Id {}", instance.getId(), session.getConnectionId());
            return WebXClientConfiguration.ForExistingSession(session.getConnectionId(), clientVersion);
        }
    }

    private WebXEngineConfiguration createEngineConfiguration() {
        final WebXEngineConfiguration config = new WebXEngineConfiguration();
        final Map<String, String> webxConfiguration = configuration.webxConfiguration();
        webxConfiguration.forEach(config::setParameter);
        return config;
    }

    private WebXHostConfiguration createHostConfiguration(final Instance instance) {
        final ImageProtocol protocol = requireNonNullElse(
            imageProtocolService.getByName("WEBX"),
            new ImageProtocol("WEBX", 5555)
        );

        String hostname = instance.getIpAddress();

        return new WebXHostConfiguration(hostname, protocol.getPort());
    }

    private WebXTunnel buildTunnel(final SocketClient client,
                                   final Instance instance) throws WebXConnectionException {
        return this.buildTunnel(client, instance, null);
    }

    private WebXTunnel buildTunnel(final SocketClient client,
                                   final Instance instance,
                                   final InstanceSession session) throws WebXConnectionException {

        final WebXHostConfiguration hostConfiguration = createHostConfiguration(instance);
        final WebXClientConfiguration clientConfiguration = createClientConfiguration(client, instance, session);
        final WebXEngineConfiguration engineConfiguration = createEngineConfiguration();

        WebXTunnel tunnel = new WebXTunnel();
        tunnel.connect(hostConfiguration, clientConfiguration, engineConfiguration);

        return tunnel;
    }

    private WebXTunnel createTunnelAndSession(final SocketClient client,
                                              final Instance instance,
                                              final ConnectedUser user) throws OwnerNotConnectedException, WebXConnectionException, WebXClientException {
        // Create new session if user is owner
        if (user.getRole().equals(InstanceMemberRole.OWNER) || instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
            final WebXTunnel tunnel = this.buildTunnel(client, instance);
            InstanceSession session = instanceSessionService.create(instance.getId(), WEBX_PROTOCOL, tunnel.getConnectionId());
            logger.info("{} created WebX session with Id {}", getInstanceAndUser(instance, user), session.getConnectionId());

            return tunnel;

        } else {
            logger.warn("A non-owner - {} - is trying to create a new instance session", getInstanceAndUser(instance, user));
            throw new OwnerNotConnectedException();
        }
    }

    private WebXTunnel getOrCreateTunnel(final SocketClient client,
                                         final Instance instance,
                                         final ConnectedUser user) throws OwnerNotConnectedException, WebXConnectionException, WebXClientException {
        InstanceSession session = instanceSessionService.getLatestByInstanceAndProtocol(instance, WEBX_PROTOCOL);

        if (session == null) {
            return this.createTunnelAndSession(client, instance, user);

        } else {
            try {
                // try to connect to existing sessionId
                logger.info("User {} connecting to existing WebX session {}", getInstanceAndUser(instance, user), session.getConnectionId());
                return buildTunnel(client, instance, session);

            } catch (WebXConnectionException exception) {
                logger.warn("Failed to connect {} to given WebX session {} so creating a new one", getInstanceAndUser(instance, user), session.getConnectionId());
                // If it fails then invalidate current session
                session.setCurrent(false);
                this.instanceSessionService.updatePartial(session);

                // Create a new session
                return this.createTunnelAndSession(client, instance, user);
            }
        }
    }
    private WebXTunnel createWebXTunnel(final SocketClient client,
                                        final Instance instance,
                                        final ConnectedUser user) throws OwnerNotConnectedException, ConnectionException {
        try {
            return getOrCreateTunnel(client, instance, user);

        } catch (WebXException exception) {
            throw new ConnectionException("Error connecting to tunnel for " + this.getInstanceAndUser(instance, user) + " : " + exception.getMessage());
        }
    }

    @Override
    public RemoteDesktopConnection connect(SocketClient client, Instance instance, ConnectedUser user) throws OwnerNotConnectedException, ConnectionException {
        final WebXTunnel webXTunnel = this.createWebXTunnel(client, instance, user);
        final ConnectionThread connectionThread = executorService.createWebXConnectionThread(client, webXTunnel, instance, user);
        return new RemoteDesktopConnection(client, connectionThread);

    }
}
