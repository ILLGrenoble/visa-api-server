package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.entity.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomLockedEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(RoomLockedEvent.class);

    private final Instance instance;

    public RoomLockedEvent(final Instance instance) {
        this.instance = instance;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Broadcasting room locked (owner away) event to make all clients read-only for instance {}", instance.getId());
        operations.sendEvent(ROOM_LOCKED_EVENT, client);
    }
}
