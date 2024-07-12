package eu.ill.visa.vdi.gateway.subscribers.events;

import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.exceptions.InvalidTokenException;
import eu.ill.visa.vdi.domain.models.*;
import eu.ill.visa.vdi.gateway.dispatcher.SocketConnectSubscriber;
import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;
import eu.ill.visa.vdi.gateway.events.ConnectionInitiatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.models.SessionEvent.ACCESS_DENIED;
import static eu.ill.visa.vdi.domain.models.SessionEvent.EVENT_CHANNEL_OPEN;

public class EventChannelConnectSubscriber implements SocketConnectSubscriber {

    private final static String PROTOCOL_PARAMETER = "protocol";

    private static final Logger logger = LoggerFactory.getLogger(EventChannelConnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final InstanceSessionService instanceSessionService;
    private final TokenAuthenticatorService authenticator;

    public EventChannelConnectSubscriber(final DesktopSessionService desktopSessionService,
                                         final InstanceSessionService instanceSessionService,
                                         final TokenAuthenticatorService authenticator) {
        this.desktopSessionService = desktopSessionService;
        this.instanceSessionService = instanceSessionService;
        this.authenticator = authenticator;
    }

    @Override
    public void onConnect(SocketClient socketClient, final NopSender nopSender) {
        final EventChannel eventChannel = new EventChannel(socketClient);

        // See if a desktop session exists already for this token (and verify that the events channel is disconnected)
        this.desktopSessionService.findDesktopSessionMemberByToken(socketClient.token()).ifPresentOrElse(desktopSessionMember -> {
            if (desktopSessionMember.isEventChannelOpen()) {
                logger.warn("Event Channel for desktop session already exists: {}. Ignoring new event channel", desktopSessionMember);

            } else {
                logger.warn("setting new Event Channel for desktop session {}", desktopSessionMember);
                desktopSessionMember.setEventChannel(eventChannel);
            }

        }, () -> {
            // If a desktop session doesn't exist then create a pending one (token is only valid once so will reject if token previously validated)
            try {
                logger.info("Initialising websocket client for Event Channel with token: {}", socketClient.token());

                final String token = socketClient.token();
                final String protocol = socketClient.getRequestParameter(PROTOCOL_PARAMETER);

                final InstanceAuthenticationToken instanceAuthenticationToken = authenticator.authenticate(token);
                final User user = instanceAuthenticationToken.getUser();
                final Instance instance = instanceAuthenticationToken.getInstance();

                final InstanceMemberRole role = instanceSessionService.getUserSessionRole(instance, user);
                ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

                PendingDesktopSessionMember pendingDesktopSessionMember = new PendingDesktopSessionMember(token, connectedUser, eventChannel, instance.getId(), protocol);
                this.desktopSessionService.addPendingDesktopSessionMember(pendingDesktopSessionMember);

                socketClient.sendEvent(new ClientEventCarrier(EVENT_CHANNEL_OPEN, new ConnectionInitiatedEvent(token)));

            } catch (InvalidTokenException exception) {
                logger.error("Token received for initialising Event Channel is invalid: {}", exception.getMessage());
                eventChannel.sendEvent(ACCESS_DENIED);
                eventChannel.disconnect();
            }
        });



    }
}
