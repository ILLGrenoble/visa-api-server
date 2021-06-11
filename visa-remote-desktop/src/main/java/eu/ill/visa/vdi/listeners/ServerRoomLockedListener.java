package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import eu.ill.visa.vdi.models.DesktopConnection;
import eu.ill.visa.vdi.services.DesktopConnectionService;

import java.util.Collection;

import static eu.ill.visa.vdi.events.Event.*;

public class ServerRoomLockedListener implements PubSubListener<DispatchMessage> {

    private final DesktopConnectionService desktopConnectionService;
    private final SocketIOServer server;

    public ServerRoomLockedListener(final DesktopConnectionService desktopConnectionService,
                                    final SocketIOServer server) {
        this.desktopConnectionService = desktopConnectionService;
        this.server = server;
    }

    @Override
    public void onMessage(final DispatchMessage message) {
        final Packet packet = message.getPacket();
        final String type = packet.getName();

        if (type != null && type.equals(ROOM_LOCKED_EVENT)) {
            final String room = message.getRoom();
            final BroadcastOperations operations = this.server.getRoomOperations(room);
            final Collection<SocketIOClient> clients = operations.getClients();

            for (final SocketIOClient aClient : clients) {
                DesktopConnection connection = this.desktopConnectionService.getDesktopConnection(aClient);
                connection.setRoomLocked(true);
            }
        }

    }
}
