package eu.ill.visa.vdi.gateway.pubsub;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import eu.ill.visa.vdi.domain.models.AccessCancellation;
import eu.ill.visa.vdi.business.services.DesktopAccessService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static eu.ill.visa.vdi.domain.events.Event.ACCESS_CANCELLATION_EVENT;

public class ServerAccessCancellationListener implements PubSubListener<DispatchMessage> {

    private final SocketIOServer server;
    private final DesktopAccessService desktopAccessService;

    public ServerAccessCancellationListener(final SocketIOServer server, final DesktopAccessService desktopAccessService) {
        this.server = server;
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onMessage(final DispatchMessage message) {
        final Packet packet = message.getPacket();
        final String type = packet.getName();

        if (type != null && type.equals(ACCESS_CANCELLATION_EVENT)) {
            final List<AccessCancellation> data = packet.getData();
            final AccessCancellation cancellation = data.get(0);
            final String room = message.getRoom();

            final BroadcastOperations operations = this.server.getRoomOperations(room);
            final Collection<SocketIOClient> clients = operations.getClients();

            this.desktopAccessService.forwardAccessCancellation(clients, cancellation.getUserFullName(), UUID.fromString(cancellation.getToken()));
        }
    }

}
