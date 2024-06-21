package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.vdi.gateway.events.AccessRequestReply;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAccessReplyListener implements DataListener<AccessRequestReply> {

    private static final Logger logger = LoggerFactory.getLogger(ClientAccessReplyListener.class);

    private final DesktopAccessService desktopAccessService;

    public ClientAccessReplyListener(final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onData(final SocketIOClient client, final AccessRequestReply data, final AckRequest ackRequest) {
        this.desktopAccessService.respondToAccessRequest(data.getInstanceId(), data.getRequesterConnectionId(), data.getRole());
    }
}
