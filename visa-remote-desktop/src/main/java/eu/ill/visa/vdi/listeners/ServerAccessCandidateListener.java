package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import eu.ill.visa.vdi.domain.AccessRequest;
import eu.ill.visa.vdi.services.DesktopAccessService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static eu.ill.visa.vdi.events.Event.ACCESS_CANDIDATE_EVENT;

public class ServerAccessCandidateListener implements PubSubListener<DispatchMessage> {

    private final SocketIOServer server;
    private final DesktopAccessService desktopAccessService;

    public ServerAccessCandidateListener(final SocketIOServer server, final DesktopAccessService desktopAccessService) {
        this.server = server;
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onMessage(final DispatchMessage message) {
        final Packet packet = message.getPacket();
        final String type = packet.getName();

        if (type != null && type.equals(ACCESS_CANDIDATE_EVENT)) {
            final List<AccessRequest> data = packet.getData();
            final AccessRequest request = data.get(0);
            final String room = message.getRoom();

            final BroadcastOperations operations = this.server.getRoomOperations(room);
            final Collection<SocketIOClient> clients = operations.getClients();

            this.desktopAccessService.forwardCandidateRequest(clients, request.getUserFullName(), UUID.fromString(request.getToken()));
        }
    }

}
