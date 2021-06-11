package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.domain.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomUnlockedEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(RoomUnlockedEvent.class);

    private final Instance instance;

    public RoomUnlockedEvent(final Instance instance) {
        this.instance = instance;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Broadcasting room unlocked (owner back) event to return clients' roles to original values for instance {}", instance.getId());
        operations.sendEvent(ROOM_UNLOCKED_EVENT, client);
    }
}
