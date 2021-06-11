package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import eu.ill.visa.vdi.domain.AccessReply;
import eu.ill.visa.vdi.services.DesktopAccessService;

import java.util.List;

import static eu.ill.visa.vdi.events.Event.ACCESS_REPLY_EVENT;

public class ServerAccessReplyListener implements PubSubListener<DispatchMessage> {

    private final DesktopAccessService desktopAccessService;

    public ServerAccessReplyListener(final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onMessage(final DispatchMessage message) {
        final Packet packet = message.getPacket();
        final String type = packet.getName();

        if (type != null && type.equals(ACCESS_REPLY_EVENT)) {
            final List<AccessReply> data = packet.getData();
            final AccessReply accessReply = data.get(0);

            this.desktopAccessService.handleForwardedAccessReply(accessReply);
        }
    }

}
