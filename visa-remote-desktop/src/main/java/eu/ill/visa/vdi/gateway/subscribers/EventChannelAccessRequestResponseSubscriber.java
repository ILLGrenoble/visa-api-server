package eu.ill.visa.vdi.gateway.subscribers;

import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.SocketEventSubscriber;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;

public class EventChannelAccessRequestResponseSubscriber implements SocketEventSubscriber<AccessRequestResponseEvent> {

    private final DesktopAccessService desktopAccessService;

    public EventChannelAccessRequestResponseSubscriber(final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onEvent(final SocketClient client, final AccessRequestResponseEvent data) {
        this.desktopAccessService.respondToAccessRequest(data.sessionId(), data.requesterConnectionId(), data.getRole());
    }
}
