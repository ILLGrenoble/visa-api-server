package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;

public class ClientAccessReplyListener implements DataListener<AccessRequestResponseEvent> {

    private final DesktopAccessService desktopAccessService;

    public ClientAccessReplyListener(final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onData(final SocketIOClient client, final AccessRequestResponseEvent data, final AckRequest ackRequest) {
        this.desktopAccessService.respondToAccessRequest(data.instanceId(), data.protocol(), data.requesterConnectionId(), data.getRole());
    }
}
