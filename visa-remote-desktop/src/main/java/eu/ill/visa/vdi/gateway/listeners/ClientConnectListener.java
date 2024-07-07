package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.SessionEventConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.models.SessionEvent.ACCESS_DENIED;
import static eu.ill.visa.vdi.domain.models.SessionEvent.OWNER_AWAY_EVENT;

public class ClientConnectListener implements ConnectListener {

    private final static String PROTOCOL_PARAMETER = "protocol";
    private final static String TOKEN_PARAMETER = "token";

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectListener.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;
    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;
    private final TokenAuthenticatorService authenticator;

    public ClientConnectListener(final DesktopSessionService desktopSessionService,
                                 final DesktopAccessService desktopAccessService,
                                 final InstanceService instanceService,
                                 final InstanceSessionService instanceSessionService,
                                 final TokenAuthenticatorService authenticator) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.authenticator = authenticator;
    }

    @Override
    public void onConnect(final SocketIOClient client) {
        final String token = client.getHandshakeData().getSingleUrlParam(TOKEN_PARAMETER);
//        final SocketClient socketClient = new SocketClient(client, token);
        final SocketClient socketClient = new SocketClient(client, client.getSessionId().toString());

        logger.info("Initialising websocket client for RemoteDesktopConnection with connection id: {} with token {}", socketClient.token(), token);

        this.desktopSessionService.getPendingDesktopSessionMember(token).ifPresentOrElse(pendingDesktopSessionMember -> {
            this.desktopSessionService.removePendingDesktopSessionMember(pendingDesktopSessionMember);

            final Long instanceId = pendingDesktopSessionMember.instanceId();
            final Instance instance = this.instanceService.getFullById(instanceId);
            if (instance != null) {
                final ConnectedUser user = pendingDesktopSessionMember.connectedUser();
                final SessionEventConnection sessionEventConnection = pendingDesktopSessionMember.sessionEventConnection();
                try {
                    if (instance.getUsername() == null) {
                        logger.warn("No username is associated with the instance {}: the owner has never connected. Disconnecting user {}", instance.getId(), user);
                        throw new OwnerNotConnectedException();

                    } else {
                        if (user.getRole().equals(InstanceMemberRole.SUPPORT)) {
                            // See if user can connect even if owner is away
                            if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
                                this.desktopSessionService.createDesktopSessionMember(socketClient, pendingDesktopSessionMember);

                            } else {
                                final InstanceSession instanceSession = this.instanceSessionService.getByInstanceAndProtocol(instance, pendingDesktopSessionMember.protocol());
                                if (instanceSession != null && this.desktopSessionService.isOwnerConnected(instanceSession.getId())) {
                                    // Start process of requesting access from the owner
                                    this.desktopAccessService.requestAccess(socketClient, instanceSession.getId(), pendingDesktopSessionMember);

                                } else {
                                    throw new OwnerNotConnectedException();
                                }
                            }

                        } else {
                            this.desktopSessionService.createDesktopSessionMember(socketClient, pendingDesktopSessionMember);
                        }
                    }

                } catch (OwnerNotConnectedException exception) {
                    sessionEventConnection.sendEvent(OWNER_AWAY_EVENT);
                    sessionEventConnection.disconnect();
                    socketClient.disconnect();

                } catch (UnauthorizedException exception) {
                    logger.warn(exception.getMessage());
                    sessionEventConnection.sendEvent(ACCESS_DENIED);
                    sessionEventConnection.disconnect();
                    socketClient.disconnect();

                } catch (ConnectionException exception) {
                    logger.error(exception.getMessage());
                    sessionEventConnection.sendEvent(ACCESS_DENIED);
                    sessionEventConnection.disconnect();
                    socketClient.disconnect();
                }

            } else {
                logger.error("Instance no longer exists for token {}", token);
                socketClient.disconnect();
            }

        }, () -> {
            logger.error("Failed to find pending desktop session connection for token {}", token);
            socketClient.disconnect();
        });
    }
}
