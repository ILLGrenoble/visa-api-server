package eu.ill.visa.vdi.services;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.vdi.concurrency.ConnectionThread;
import eu.ill.visa.vdi.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.exceptions.ConnectionException;
import eu.ill.visa.vdi.exceptions.OwnerNotConnectedException;
import eu.ill.webx.WebXClientInformation;
import eu.ill.webx.WebXConfiguration;
import eu.ill.webx.WebXTunnel;
import eu.ill.webx.exceptions.WebXClientException;
import eu.ill.webx.exceptions.WebXConnectionException;
import eu.ill.webx.exceptions.WebXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.Role.OWNER;
import static java.util.Objects.requireNonNullElse;

public class WebXDesktopService extends DesktopService {

    private static final Logger logger = LoggerFactory.getLogger(WebXDesktopService.class);

    private final InstanceSessionService instanceSessionService;
    private final SignatureService signatureService;
    private final ImageProtocolService imageProtocolService;
    private final ConnectionThreadExecutor executorService;

    public WebXDesktopService(final InstanceSessionService instanceSessionService,
                              final CloudClientGateway cloudClientGateway,
                              final SignatureService signatureService,
                              final ImageProtocolService imageProtocolService,
                              final ConnectionThreadExecutor executorService) {
        super(cloudClientGateway);
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

    private WebXConfiguration createConfiguration(final Instance instance) throws CloudException {
        final ImageProtocol protocol = requireNonNullElse(
            imageProtocolService.getByName("WEBX"),
            new ImageProtocol("WEBX", 5555)
        );

        String hostname = getIpAddressForInstance(instance);

        return new WebXConfiguration(hostname, protocol.getPort());
    }

    private WebXTunnel buildTunnel(final Instance instance) throws CloudException, WebXConnectionException {
        return this.buildTunnel(instance, null);
    }

    private WebXTunnel buildTunnel(final Instance instance,
                                   final InstanceSession session) throws CloudException, WebXConnectionException {

        final WebXConfiguration configuration = createConfiguration(instance);

        WebXClientInformation clientInformation = createClientInformation(session, instance);

        WebXTunnel tunnel = new WebXTunnel();
        tunnel.connect(configuration, clientInformation);

        return tunnel;
    }

    private WebXTunnel createTunnelAndSession(Instance instance, User user, Role role) throws OwnerNotConnectedException, CloudException, WebXConnectionException, WebXClientException {
        // Create new session if user is owner
        if (role.equals(OWNER) || instanceSessionService.canConnectWhileOwnerAway(instance, user)) {
            final WebXTunnel tunnel = buildTunnel(instance);
            InstanceSession session = instanceSessionService.create(instance, tunnel.getConnectionId());
            logger.info("User {} created WebX session {}", getInstanceAndUser(instance, user, role), session.getConnectionId());

            return tunnel;

        } else {
            logger.warn("A non-owner - {} - is trying to create a new instance session", getInstanceAndUser(instance, user, role));
            throw new OwnerNotConnectedException();
        }
    }

    private WebXTunnel getOrCreateTunnel(Instance instance, User user, Role role) throws OwnerNotConnectedException, WebXConnectionException, WebXClientException, CloudException {
        InstanceSession session = instanceSessionService.getByInstance(instance);

        if (session == null) {
            return this.createTunnelAndSession(instance, user, role);

        } else {
            try {
                // try to connect to existing sessionId
                logger.info("User {} connecting to existing WebX session {}", getInstanceAndUser(instance, user, role), session.getConnectionId());
                return buildTunnel(instance, session);

            } catch (WebXConnectionException exception) {
                logger.error("Failed to connect {} to given WebX session {} so creating a new one", getInstanceAndUser(instance, user, role), session.getConnectionId());
                // If it fails then invalidate current session
                session.setCurrent(false);
                this.instanceSessionService.save(session);

                // Create a new session
                return this.createTunnelAndSession(instance, user, role);
            }
        }
    }
    private WebXTunnel createWebXTunnel(final Instance instance,
                                        final User user,
                                        final Role role) throws OwnerNotConnectedException, ConnectionException {
        try {
            return getOrCreateTunnel(instance, user, role);

        } catch (WebXException exception) {
            throw new ConnectionException("Error connecting to tunnel for " + this.getInstanceAndUser(instance, user, role) + " : " + exception.getMessage());

        } catch (CloudException exception) {
            throw new ConnectionException("There was an exception contacting the cloud for : " + this.getInstanceAndUser(instance, user, role) + " : " + exception.getMessage());
        }
    }

    @Override
    public ConnectionThread connect(SocketIOClient client, Instance instance, User user, Role role) throws OwnerNotConnectedException, ConnectionException {
        final WebXTunnel webXTunnel = this.createWebXTunnel(instance, user, role);
        return executorService.startWebXConnectionThread(client, webXTunnel, instance, user, role);
    }
}
