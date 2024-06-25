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
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.models.Event.ACCESS_DENIED;
import static eu.ill.visa.vdi.domain.models.Event.OWNER_AWAY_EVENT;

public class ClientConnectListener implements ConnectListener {

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
        try {
            logger.info("Initialising websocket client with session id: {}", client.getSessionId());

            final InstanceAuthenticationToken token = authenticator.authenticate(client);

            final User user = token.getUser();
            final Instance instance = token.getInstance();
            final InstanceMemberRole role = instanceSessionService.getUserSessionRole(instance, user);
            ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

            if (instance.getUsername() == null) {
                logger.warn("No username is associated with the instance {}: the owner has never connected. Disconnecting user {}", instance.getId(), user);
                throw new OwnerNotConnectedException();

            } else {
                if (role.equals(InstanceMemberRole.SUPPORT)) {
                    // See if user can connect even if owner is away
                    if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
                        this.desktopConnectionService.createDesktopConnection(client, instance, connectedUser);

                    } else {
                        if (this.desktopConnectionService.isOwnerConnected(instance)) {
                            // Start process of requesting access from the owner
                            this.desktopAccessService.requestAccess(client, connectedUser, instance.getId());

                        } else {
                            throw new OwnerNotConnectedException();
                        }
                    }

                } else {
                    this.desktopConnectionService.createDesktopConnection(client, instance, connectedUser);
                }
            }

        } catch (OwnerNotConnectedException exception) {
            client.sendEvent(OWNER_AWAY_EVENT);
            client.disconnect();

        } catch (UnauthorizedException exception) {
            logger.warn(exception.getMessage());
            client.sendEvent(ACCESS_DENIED);
            client.disconnect();

        } catch (InvalidTokenException | ConnectionException exception) {
            logger.error(exception.getMessage());
            client.disconnect();
        }
    }


}
