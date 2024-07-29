package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.business.services.InstanceAuthenticationTokenService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.NopSender;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.models.SessionEvent.ACCESS_DENIED;
import static eu.ill.visa.vdi.domain.models.SessionEvent.OWNER_AWAY_EVENT;

public class RemoteDesktopConnectSubscriber {

    public final static String TOKEN_PATH_PARAMETER = "token";

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopConnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;
    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceAuthenticationTokenService authenticator;
    private final EventDispatcher eventDispatcher;

    public RemoteDesktopConnectSubscriber(final DesktopSessionService desktopSessionService,
                                          final DesktopAccessService desktopAccessService,
                                          final InstanceService instanceService,
                                          final InstanceSessionService instanceSessionService,
                                          final InstanceAuthenticationTokenService authenticator,
                                          final EventDispatcher eventDispatcher) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.authenticator = authenticator;
        this.eventDispatcher = eventDispatcher;
    }

    public void onConnect(final SocketClient socketClient, final NopSender nopSender) {

        logger.info("Initialising websocket client for RemoteDesktopConnection with clientId {}", socketClient.clientId());

        try {
            // Validate the token and get the user and instance
            final String token = socketClient.getPathParameter(TOKEN_PATH_PARAMETER);
            final InstanceAuthenticationToken instanceAuthenticationToken = authenticator.authenticate(token);
            final User user = instanceAuthenticationToken.getUser();
            final Instance instance = instanceAuthenticationToken.getInstance();

            final InstanceMemberRole role = instanceSessionService.getUserSessionRole(instance, user);
            ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

            if (instance.getUsername() == null) {
                logger.warn("No username is associated with the instance {}: the owner has never connected. Disconnecting user {}", instance.getId(), user);
                throw new OwnerNotConnectedException();

            } else {
                if (connectedUser.getRole().equals(InstanceMemberRole.SUPPORT)) {
                    // See if user can connect even if owner is away
                    if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
                        this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance);

                    } else {
                        final InstanceSession instanceSession = this.instanceSessionService.getByInstanceAndProtocol(instance, socketClient.protocol());
                        if (instanceSession != null && this.desktopSessionService.isOwnerConnected(instanceSession.getId())) {
                            // Start process of requesting access from the owner
                            this.desktopAccessService.requestAccess(socketClient, instanceSession.getId(), connectedUser, instance.getId(), nopSender);

                        } else {
                            throw new OwnerNotConnectedException();
                        }
                    }

                } else {
                    this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance);
                }
            }


        } catch (InvalidTokenException exception) {
            logger.error("Token received for initialising RemoteDesktopConnection is invalid: {}", exception.getMessage());
            this.eventDispatcher.sendEventToClient(socketClient.clientId(), ACCESS_DENIED);
            socketClient.disconnect();

        } catch (OwnerNotConnectedException exception) {
            this.eventDispatcher.sendEventToClient(socketClient.clientId(), OWNER_AWAY_EVENT);
            socketClient.disconnect();

        } catch (UnauthorizedException exception) {
            logger.warn(exception.getMessage());
            this.eventDispatcher.sendEventToClient(socketClient.clientId(), ACCESS_DENIED);
            socketClient.disconnect();

        } catch (ConnectionException exception) {
            logger.error(exception.getMessage());
            this.eventDispatcher.sendEventToClient(socketClient.clientId(), ACCESS_DENIED);
            socketClient.disconnect();
        }
    }
}
