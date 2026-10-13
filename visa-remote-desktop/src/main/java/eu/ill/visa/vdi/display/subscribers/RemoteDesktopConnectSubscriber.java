package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.vdi.apiconnect.exceptions.InvalidTokenException;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.business.services.VisaApiService;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.NopSender;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.common.utils.exceptions.ExceptionFormatter.formatException;
import static eu.ill.visa.vdi.domain.models.SessionEvent.*;

public class RemoteDesktopConnectSubscriber {

    public final static String TOKEN_PATH_PARAMETER = "token";

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopConnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;
    private final EventDispatcher eventDispatcher;
    private final VisaApiService visaApiService;

    public RemoteDesktopConnectSubscriber(final DesktopSessionService desktopSessionService,
                                          final DesktopAccessService desktopAccessService,
                                          final EventDispatcher eventDispatcher,
                                          final VisaApiService visaApiService) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
        this.eventDispatcher = eventDispatcher;
        this.visaApiService = visaApiService;
    }

    public void onConnect(final SocketClient socketClient, final NopSender nopSender) {

        logger.info("Initialising websocket client for RemoteDesktopConnection with clientId {}", socketClient.clientId());

        try {
            // Validate the token and get the user and instance
            final String token = socketClient.getPathParameter(TOKEN_PATH_PARAMETER);
            final VdiAuthenticationToken instanceAuthenticationToken = this.visaApiService.authenticate(token, socketClient.protocol());
            final ConnectedUser connectedUser = instanceAuthenticationToken.connectedUser();
            final VdiInstance instance = instanceAuthenticationToken.instance();
            final Boolean isPublicAccessToken = instanceAuthenticationToken.isPublicAccessToken();
            final String activeConnectionId = instanceAuthenticationToken.activeConnectionId();
            final Boolean isOwnerConnected = instanceAuthenticationToken.isOwnerConnected();


            if (connectedUser.role().equals(InstanceMemberRole.SUPPORT)) {
                // See if user can connect even if owner is away
                if (instance.canConnectWhileOwnerAway()) {
                    this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance, activeConnectionId, nopSender);

                } else {
                    if (activeConnectionId != null && isOwnerConnected) {
                        // Start process of requesting access from the owner
                        this.desktopAccessService.requestAccess(socketClient, activeConnectionId, connectedUser, instance, nopSender);

                    } else {
                        throw new OwnerNotConnectedException();
                    }
                }

            } else if (isPublicAccessToken) {
                // If using a public access token then the owner must be connected
                if (isOwnerConnected) {
                    this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance, activeConnectionId, nopSender);

                } else {
                    throw new OwnerNotConnectedException();
                }

            } else {
                this.desktopSessionService.createDesktopSessionMember(socketClient, connectedUser, instance, activeConnectionId, nopSender);
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

        } catch (Exception exception) {
            logger.error(formatException(exception));
            this.eventDispatcher.sendEventToClient(socketClient.clientId(), USER_DISCONNECTED_EVENT);
            socketClient.disconnect();
        }
    }
}
