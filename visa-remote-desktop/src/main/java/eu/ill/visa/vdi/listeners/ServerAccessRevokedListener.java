package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static eu.ill.visa.vdi.events.Event.ACCESS_REVOKED_EVENT;

public class ServerAccessRevokedListener implements PubSubListener<DispatchMessage> {

    private static final Logger logger = LoggerFactory.getLogger(ServerAccessRevokedListener.class);

    private final SocketIOServer server;

    public ServerAccessRevokedListener(final SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void onMessage(final DispatchMessage message) {
        final Packet packet = message.getPacket();
        final String type = packet.getName();

        if (type != null && type.equals(ACCESS_REVOKED_EVENT)) {

            final List<Object> data = packet.getData();
            final String roomId = (String)data.get(0);
            final UUID revokedClientSessionId = (UUID)data.get(1);

            BroadcastOperations operations = this.server.getRoomOperations(roomId);
            if (operations != null) {
                final Collection<SocketIOClient> clients = operations.getClients();

                Optional<SocketIOClient> clientOptional = clients.stream()
                    .filter(aClient -> aClient.getSessionId().equals(revokedClientSessionId))
                    .findFirst();

                if (clientOptional.isPresent()) {
                    SocketIOClient client = clientOptional.get();
                    logger.info("Revoking access to remote desktop for instance {} for client with ID {}", roomId, revokedClientSessionId);
                    client.sendEvent(ACCESS_REVOKED_EVENT);
                    client.disconnect();
                }
            }

        }
    }

}
