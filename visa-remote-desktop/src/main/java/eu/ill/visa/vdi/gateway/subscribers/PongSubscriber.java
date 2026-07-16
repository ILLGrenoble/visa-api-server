package eu.ill.visa.vdi.gateway.subscribers;

import eu.ill.visa.broker.gateway.ClientEventSubscriber;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.gateway.events.PongEvent;

import java.util.Date;

public class PongSubscriber implements ClientEventSubscriber<PongEvent> {

    private final DesktopSessionService desktopSessionService;

    public PongSubscriber(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    @Override
    public void onEvent(final String clientId, final PongEvent pongEvent) {
        Date now = new Date();
        this.desktopSessionService.getDesktopSessionMember(clientId).ifPresent(desktopSessionMember -> {
            this.desktopSessionService.onPongReceivedFromClient(desktopSessionMember, now.getTime() - pongEvent.time());
        });
    }
}
