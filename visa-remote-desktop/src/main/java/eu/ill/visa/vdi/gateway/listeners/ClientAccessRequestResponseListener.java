package eu.ill.visa.vdi.gateway.listeners;

import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.ClientEventSubscriber;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;

public class ClientAccessRequestResponseListener implements ClientEventSubscriber<AccessRequestResponseEvent> {

    private final DesktopAccessService desktopAccessService;

    public ClientAccessRequestResponseListener(final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onEvent(final SocketClient client, final AccessRequestResponseEvent data) {
        this.desktopAccessService.respondToAccessRequest(data.sessionId(), data.requesterConnectionId(), data.getRole());
    }
}
