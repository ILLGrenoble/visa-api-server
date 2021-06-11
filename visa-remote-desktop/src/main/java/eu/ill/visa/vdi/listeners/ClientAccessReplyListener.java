package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.vdi.domain.AccessReply;
import eu.ill.visa.vdi.services.DesktopAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAccessReplyListener implements DataListener<AccessReply> {

    private static final Logger logger = LoggerFactory.getLogger(ClientAccessReplyListener.class);

    private final DesktopAccessService desktopAccessService;

    public ClientAccessReplyListener(final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onData(final SocketIOClient client, final AccessReply data, final AckRequest ackRequest) {
        this.desktopAccessService.onAccessReply(client, data);
    }
}
