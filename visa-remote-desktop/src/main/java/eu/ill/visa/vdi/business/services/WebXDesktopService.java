package eu.ill.visa.vdi.business.services;

import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.webx.WebXClientInformation;
import eu.ill.webx.WebXConfiguration;
import eu.ill.webx.WebXTunnel;
import eu.ill.webx.exceptions.WebXClientException;
import eu.ill.webx.exceptions.WebXConnectionException;
import eu.ill.webx.exceptions.WebXException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNullElse;

@ApplicationScoped
public class WebXDesktopService extends DesktopService {

    private static final Logger logger = LoggerFactory.getLogger(WebXDesktopService.class);

    private final InstanceSessionService instanceSessionService;
    private final SignatureService signatureService;
    private final ImageProtocolService imageProtocolService;
    private final ConnectionThreadExecutor executorService;

    @Inject
    public WebXDesktopService(final InstanceSessionService instanceSessionService,
                              final SignatureService signatureService,
                              final ImageProtocolService imageProtocolService,
                              final ConnectionThreadExecutor executorService) {
        this.instanceSessionService = instanceSessionService;
        this.signatureService = signatureService;
        this.imageProtocolService = imageProtocolService;
        this.executorService = executorService;
    }

    private WebXClientInformation createClientInformation(final InstanceSession session, final Instance instance) {
        if (session == null) {
            // use session Id to connect to remote desktop
        }
        final Integer screenHeight = instance.getScreenHeight();
        final Integer screenWidth = instance.getScreenWidth();
        String username = instance.getUsername();
        logger.info("Creating new WebX session on instance {} with username {}", instance.getId(), username);
        String autologin = instance.getPlan().getImage().getAutologin();
        String password = null;
        if (autologin != null && autologin.equals("VISA_PAM")) {
            password =  signatureService.createSignature(username);
        }
        String keyboardLayout = instance.getKeyboardLayout();

        return new WebXClientInformation(username, password, screenWidth, screenHeight, keyboardLayout);
    }

    private WebXConfiguration createConfiguration(final Instance instance) {
        final ImageProtocol protocol = requireNonNullElse(
            imageProtocolService.getByName("WEBX"),
            new ImageProtocol("WEBX", 5555)
        );

        String hostname = instance.getIpAddress();

        return new WebXConfiguration(hostname, protocol.getPort());
    }

    private WebXTunnel buildTunnel(final Instance instance) throws WebXConnectionException {
        return this.buildTunnel(instance, null);
    }

    private WebXTunnel buildTunnel(final Instance instance,
                                   final InstanceSession session) throws WebXConnectionException {

        final WebXConfiguration configuration = createConfiguration(instance);

        WebXClientInformation clientInformation = createClientInformation(session, instance);

        WebXTunnel tunnel = new WebXTunnel();
        tunnel.connect(configuration, clientInformation);

        return tunnel;
    }

    private WebXTunnel createTunnelAndSession(Instance instance, ConnectedUser user) throws OwnerNotConnectedException, WebXConnectionException, WebXClientException {
        // Create new session if user is owner
        if (user.getRole().equals(InstanceMemberRole.OWNER) || instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
            final WebXTunnel tunnel = buildTunnel(instance);
            InstanceSession session = instanceSessionService.create(instance, WEBX_PROTOCOL, tunnel.getConnectionId());
            logger.info("User {} created WebX session {}", getInstanceAndUser(instance, user), session.getConnectionId());

            return tunnel;

        } else {
            logger.warn("A non-owner - {} - is trying to create a new instance session", getInstanceAndUser(instance, user));
            throw new OwnerNotConnectedException();
        }
    }

    private WebXTunnel getOrCreateTunnel(Instance instance, ConnectedUser user) throws OwnerNotConnectedException, WebXConnectionException, WebXClientException {
        InstanceSession session = instanceSessionService.getByInstanceAndProtocol(instance, WEBX_PROTOCOL);

        if (session == null) {
            return this.createTunnelAndSession(instance, user);

        } else {
            try {
                // try to connect to existing sessionId
                logger.info("User {} connecting to existing WebX session {}", getInstanceAndUser(instance, user), session.getConnectionId());
                return buildTunnel(instance, session);

            } catch (WebXConnectionException exception) {
                logger.error("Failed to connect {} to given WebX session {} so creating a new one", getInstanceAndUser(instance, user), session.getConnectionId());
                // If it fails then invalidate current session
                session.setCurrent(false);
                this.instanceSessionService.save(session);

                // Create a new session
                return this.createTunnelAndSession(instance, user);
            }
        }
    }
    private WebXTunnel createWebXTunnel(final Instance instance,
                                        final ConnectedUser user) throws OwnerNotConnectedException, ConnectionException {
        try {
            return getOrCreateTunnel(instance, user);

        } catch (WebXException exception) {
            throw new ConnectionException("Error connecting to tunnel for " + this.getInstanceAndUser(instance, user) + " : " + exception.getMessage());
        }
    }

    @Override
    public RemoteDesktopConnection connect(SocketClient client, Instance instance, ConnectedUser user) throws OwnerNotConnectedException, ConnectionException {
        final WebXTunnel webXTunnel = this.createWebXTunnel(instance, user);
        final ConnectionThread connectionThread = executorService.startWebXConnectionThread(client, webXTunnel, instance, user);
        return new RemoteDesktopConnection(client, connectionThread);

    }
}
