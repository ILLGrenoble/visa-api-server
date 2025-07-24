package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.business.services.InstanceAuthenticationTokenService;
import eu.ill.visa.business.services.InstanceSessionMemberService;
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

import static eu.ill.visa.vdi.domain.models.SessionEvent.*;

public class RemoteDesktopConnectSubscriber {

    public final static String TOKEN_PATH_PARAMETER = "token";

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopConnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceSessionMemberService instanceSessionMemberService;
    private final InstanceAuthenticationTokenService authenticator;
    private final EventDispatcher eventDispatcher;

    public RemoteDesktopConnectSubscriber(final DesktopSessionService desktopSessionService,
                                          final DesktopAccessService desktopAccessService,
                                          final InstanceSessionService instanceSessionService,
                                          final InstanceSessionMemberService instanceSessionMemberService,
                                          final InstanceAuthenticationTokenService authenticator,
                                          final EventDispatcher eventDispatcher) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
        this.instanceSessionService = instanceSessionService;
        this.instanceSessionMemberService = instanceSessionMemberService;
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
            final String publicAccessToken = instanceAuthenticationToken.getPublicAccessToken();

            InstanceMemberRole role = instanceSessionService.getUserSessionRole(instance, user);
            boolean usingPublicAccessToken = false;

            // Check if we should examine the accessToken
            if (role.equals(InstanceMemberRole.NONE) || role.equals(InstanceMemberRole.SUPPORT) && publicAccessToken != null && publicAccessToken.equals(instance.getPublicAccessToken())) {
                logger.info("User {} is connecting using a public access token for instance {}", user, instance.getId());
                role = instance.getPublicAccessRole();
                usingPublicAccessToken = true;
            }

            ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

            if (connectedUser.getRole().equals(InstanceMemberRole.SUPPORT)) {
                // See if user can connect even if owner is away
                if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user)) {
                    this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance, nopSender);

                } else {
                    final InstanceSession instanceSession = this.instanceSessionService.getLatestByInstanceAndProtocol(instance, socketClient.protocol());
                    if (instanceSession != null && this.desktopSessionService.isOwnerConnected(instanceSession.getId())) {
                        // Start process of requesting access from the owner
                        this.desktopAccessService.requestAccess(socketClient, instanceSession.getId(), connectedUser, instance.getId(), nopSender);

                    } else {
                        throw new OwnerNotConnectedException();
                    }
                }

            } else if (usingPublicAccessToken) {
                // If using a public access token then the owner must be connected
                if (this.instanceSessionMemberService.isOwnerConnected(instance, socketClient.protocol())) {
                    this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance, nopSender);

                } else {
                    throw new OwnerNotConnectedException();
                }

            } else {
                this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance, nopSender);
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
            this.eventDispatcher.sendEventToClient(socketClient.clientId(), USER_DISCONNECTED_EVENT);
            socketClient.disconnect();
        }
    }
}
