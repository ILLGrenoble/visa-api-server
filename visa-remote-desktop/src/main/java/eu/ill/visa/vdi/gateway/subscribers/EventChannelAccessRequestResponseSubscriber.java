package eu.ill.visa.vdi.gateway.subscribers;

import eu.ill.visa.broker.gateway.ClientEventSubscriber;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;

public class EventChannelAccessRequestResponseSubscriber implements ClientEventSubscriber<AccessRequestResponseEvent> {

    private final DesktopAccessService desktopAccessService;
    private final DesktopSessionService desktopSessionService;

    public EventChannelAccessRequestResponseSubscriber(final DesktopAccessService desktopAccessService,
                                                       final DesktopSessionService desktopSessionService) {
        this.desktopAccessService = desktopAccessService;
        this.desktopSessionService = desktopSessionService;
    }

    @Override
    public void onEvent(final String clientId, final AccessRequestResponseEvent data) {
        this.desktopSessionService.findDesktopSessionMemberByClientId(clientId).ifPresent(desktopSessionMember -> {
            this.desktopAccessService.respondToAccessRequest(desktopSessionMember, data.sessionId(), data.requesterConnectionId(), data.getRole());
        });

    }
}
