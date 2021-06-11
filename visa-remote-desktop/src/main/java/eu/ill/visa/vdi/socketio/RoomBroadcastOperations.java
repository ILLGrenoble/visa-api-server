package eu.ill.visa.vdi.socketio;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.protocol.PacketType;
import com.corundumstudio.socketio.store.StoreFactory;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is a fix for the default socket.io room broadcast operations. This fixes
 * the send function from sending the message to everyone that is in any room that
 * the client is in, essentially it actually uses the room instead of ignoring it.
 *
 * https://github.com/mrniko/netty-socketio/issues/462
 */
public class RoomBroadcastOperations extends BroadcastOperations {
    private final List<String> rooms;
    private final StoreFactory storeFactory;

    public RoomBroadcastOperations(final Collection<SocketIOClient> clients, final StoreFactory storeFactory, final String... rooms) {
        super(clients, storeFactory);

        this.storeFactory = storeFactory;
        this.rooms = Arrays.asList(rooms);
    }

    @Override
    public void sendEvent(String name, SocketIOClient excludedClient, Object... data) {
        Packet packet = new Packet(PacketType.MESSAGE);
        packet.setSubType(PacketType.EVENT);
        packet.setName(name);
        packet.setData(Arrays.asList(data));
        Iterator clientIterator = this.getClients().iterator();

        while(clientIterator.hasNext()) {
            SocketIOClient client = (SocketIOClient)clientIterator.next();
            if (!client.getSessionId().equals(excludedClient.getSessionId())) {
                client.send(packet);
            }
        }

        this.roomDispatch(packet);
    }

    @Override
    public void send(Packet packet) {
        getClients().forEach(c -> c.send(packet));

        this.roomDispatch(packet);
    }

    private void roomDispatch(Packet packet) {
        // Do it only to the specific rooms, and not all of the rooms that the user is in,
        // like the default implementation does.
        rooms.forEach(room ->
            storeFactory.pubSubStore().publish(
                PubSubType.DISPATCH,
                new DispatchMessage(room, packet, "")
            ));
    }
}
