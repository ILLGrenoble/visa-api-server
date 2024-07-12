package eu.ill.visa.vdi.gateway.subscribers;

import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.SocketDisconnectSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventChannelDisconnectSubscriber implements SocketDisconnectSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(EventChannelDisconnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;

    public EventChannelDisconnectSubscriber(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    @Override
    public void onDisconnect(SocketClient socketClient) {
        // See if the desktop session is pending
        this.desktopSessionService.getPendingDesktopSessionMember(socketClient.token()).ifPresentOrElse(pendingDesktopSessionMember -> {
            logger.info("Event Channel disconnected by client {}: removing pending Session Member.", socketClient.token());
            this.desktopSessionService.removePendingDesktopSessionMember(pendingDesktopSessionMember);
        }, () -> {

            // If not pending then disable the event channel (client should try to reconnect event channel)
            this.desktopSessionService.findDesktopSessionMemberByToken(socketClient.token()).ifPresent(desktopSessionMember -> {
                logger.info("Event Channel disconnected by client {}: disabling event channel in session member: {}", socketClient.token(), desktopSessionMember);
                desktopSessionMember.setEventChannel(null);
            });
        });
    }
}

