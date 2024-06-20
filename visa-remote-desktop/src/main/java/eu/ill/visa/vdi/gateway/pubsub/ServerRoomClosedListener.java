package eu.ill.visa.vdi.gateway.pubsub;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;

import java.util.Collection;

import static eu.ill.visa.vdi.domain.events.Event.OWNER_AWAY_EVENT;
import static eu.ill.visa.vdi.domain.events.Event.ROOM_CLOSED_EVENT;

public class ServerRoomClosedListener implements PubSubListener<DispatchMessage> {

    private final SocketIOServer server;

    public ServerRoomClosedListener(final SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void onMessage(final DispatchMessage message) {
        final Packet packet = message.getPacket();
        final String type = packet.getName();

        if (type != null && type.equals(ROOM_CLOSED_EVENT)) {
            final String room = message.getRoom();
            final BroadcastOperations operations = this.server.getRoomOperations(room);
            final Collection<SocketIOClient> clients = operations.getClients();

            for (final SocketIOClient aClient : clients) {
                aClient.sendEvent(OWNER_AWAY_EVENT);
                aClient.disconnect();
            }
        }

    }
}
