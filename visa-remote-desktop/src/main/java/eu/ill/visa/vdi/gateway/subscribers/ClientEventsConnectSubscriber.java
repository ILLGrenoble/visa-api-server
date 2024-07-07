package eu.ill.visa.vdi.gateway.subscribers;

import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.exceptions.InvalidTokenException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.PendingDesktopSessionMember;
import eu.ill.visa.vdi.domain.models.SessionEventConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.ClientConnectSubscriber;
import eu.ill.visa.vdi.gateway.events.ConnectionInitiatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.models.SessionEvent.ACCESS_DENIED;
import static eu.ill.visa.vdi.domain.models.SessionEvent.CONNECTION_INITIATED_EVENT;

public class ClientEventsConnectSubscriber implements ClientConnectSubscriber {

    private final static String PROTOCOL_PARAMETER = "protocol";

    private static final Logger logger = LoggerFactory.getLogger(ClientEventsConnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final InstanceSessionService instanceSessionService;
    private final TokenAuthenticatorService authenticator;

    public ClientEventsConnectSubscriber(final DesktopSessionService desktopSessionService,
                                         final InstanceSessionService instanceSessionService,
                                         final TokenAuthenticatorService authenticator) {
        this.desktopSessionService = desktopSessionService;
        this.instanceSessionService = instanceSessionService;
        this.authenticator = authenticator;
    }

    @Override
    public void onConnect(SocketClient socketClient) {
        final SessionEventConnection sessionEventConnection = new SessionEventConnection(socketClient);

        // See if a desktop session exists already for this token (and verify that the events channel is disconnected)
        this.desktopSessionService.findDesktopSessionMemberByToken(socketClient.token()).ifPresentOrElse(desktopSessionMember -> {
            if (desktopSessionMember.isEventConnectionOpen()) {
                logger.warn("Event connection for desktop session already exists: {}. Ignoring new event connection", desktopSessionMember);

            } else {
                logger.warn("setting new Event Connection for desktop session {}", desktopSessionMember);
                desktopSessionMember.setEventConnection(sessionEventConnection);
            }

        }, () -> {
            // If a desktop session doesn't exist then create a pending one (token is only valid once so will reject if token previously validated)
            try {
                logger.info("Initialising websocket client for SessionEventConnection with connection id: {}", socketClient.token());

                final String token = socketClient.token();
                final String protocol = socketClient.getRequestParameter(PROTOCOL_PARAMETER);

                final InstanceAuthenticationToken instanceAuthenticationToken = authenticator.authenticate(token);
                final User user = instanceAuthenticationToken.getUser();
                final Instance instance = instanceAuthenticationToken.getInstance();

                final InstanceMemberRole role = instanceSessionService.getUserSessionRole(instance, user);
                ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

                PendingDesktopSessionMember pendingDesktopSessionMember = new PendingDesktopSessionMember(token, connectedUser, sessionEventConnection, instance.getId(), protocol);
                this.desktopSessionService.addPendingDesktopSessionMember(pendingDesktopSessionMember);

                socketClient.sendEvent(CONNECTION_INITIATED_EVENT, new ConnectionInitiatedEvent(token));

            } catch (InvalidTokenException exception) {
                logger.error("Token received for initialising Desktop Connection is invalid: {}", exception.getMessage());
                sessionEventConnection.sendEvent(ACCESS_DENIED);
                sessionEventConnection.disconnect();
            }
        });



    }
}
