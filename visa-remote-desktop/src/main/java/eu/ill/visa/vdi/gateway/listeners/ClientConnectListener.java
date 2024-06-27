package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.InvalidTokenException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.models.SessionEvent.ACCESS_DENIED;
import static eu.ill.visa.vdi.domain.models.SessionEvent.OWNER_AWAY_EVENT;

public class ClientConnectListener implements ConnectListener {

    private final static String PROTOCOL_PARAMETER = "protocol";
    private final static String TOKEN_PARAMETER = "token";

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectListener.class);

    private final DesktopConnectionService desktopConnectionService;
    private final DesktopAccessService desktopAccessService;
    private final InstanceSessionService instanceSessionService;
    private final TokenAuthenticatorService authenticator;

    public ClientConnectListener(final DesktopConnectionService desktopConnectionService,
                                 final DesktopAccessService desktopAccessService,
                                 final InstanceSessionService instanceSessionService,
                                 final TokenAuthenticatorService authenticator) {
        this.desktopConnectionService = desktopConnectionService;
        this.desktopAccessService = desktopAccessService;
        this.instanceSessionService = instanceSessionService;
        this.authenticator = authenticator;
    }

    @Override
    public void onConnect(final SocketIOClient client) {
        this.initialiseDesktopSessionMember(client);
        this.createRemoteDesktopConnection(client);
    }

    private void initialiseDesktopSessionMember(final SocketIOClient client) {
        String connectionId = client.getSessionId().toString();
        final SocketClient socketClient = new SocketClient(client, connectionId);
        final SessionEventConnection sessionEventConnection = new SessionEventConnection(socketClient);

        logger.info("Initialising websocket client for SessionEventConnection with connection id: {}", connectionId);

        final String token = client.getHandshakeData().getSingleUrlParam(TOKEN_PARAMETER);
        try {
            final InstanceAuthenticationToken instanceAuthenticationToken = authenticator.authenticate(token);
            final User user = instanceAuthenticationToken.getUser();
            final Instance instance = instanceAuthenticationToken.getInstance();

            final InstanceMemberRole role = instanceSessionService.getUserSessionRole(instance, user);
            ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

            final String protocol = client.getHandshakeData().getSingleUrlParam(PROTOCOL_PARAMETER);

            PendingDesktopSessionMember pendingDesktopSessionMember = new PendingDesktopSessionMember(connectedUser, sessionEventConnection, instance, protocol, token);
            this.desktopConnectionService.addPendingDesktopSessionMember(pendingDesktopSessionMember);

        } catch (InvalidTokenException exception) {
            logger.error("Token received for initialising Desktop Connection is invalid: {}", exception.getMessage());
            sessionEventConnection.sendEvent(ACCESS_DENIED);
            sessionEventConnection.disconnect();
        }

    }

    private void createRemoteDesktopConnection(final SocketIOClient client) {
        String connectionId = client.getSessionId().toString();
        final SocketClient socketClient = new SocketClient(client, connectionId);
        final String token = client.getHandshakeData().getSingleUrlParam(TOKEN_PARAMETER);

        logger.info("Initialising websocket client for RemoteDesktopConnection with connection id: {} with token {}", connectionId, token);

        final PendingDesktopSessionMember pendingDesktopSessionMember = this.desktopConnectionService.getPendingDesktopSessionMember(token);
        if (pendingDesktopSessionMember != null) {
            final Instance instance = pendingDesktopSessionMember.instance();
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
                            this.desktopConnectionService.createDesktopSessionMember(socketClient, pendingDesktopSessionMember);

                        } else {
                            if (this.desktopConnectionService.isOwnerConnected(instance.getId(), pendingDesktopSessionMember.protocol())) {
                                // Start process of requesting access from the owner
                                this.desktopAccessService.requestAccess(socketClient, pendingDesktopSessionMember);

                            } else {
                                throw new OwnerNotConnectedException();
                            }
                        }

                    } else {
                        this.desktopConnectionService.createDesktopSessionMember(socketClient, pendingDesktopSessionMember);
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
            logger.error("Failed to find pending desktop session connection for token {}", token);
            socketClient.disconnect();
        }
    }
}
