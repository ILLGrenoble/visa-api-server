package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AccessRevokedEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(AccessRevokedEvent.class);

    private String room;
    private UUID clientSessionId;

    public AccessRevokedEvent(String room, UUID clientSessionId) {
        this.room = room;
        this.clientSessionId = clientSessionId;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Broadcasting access revoked event for client {}", this.clientSessionId);
        operations.sendEvent(ACCESS_REVOKED_EVENT, client, this.room, this.clientSessionId);
    }
}
