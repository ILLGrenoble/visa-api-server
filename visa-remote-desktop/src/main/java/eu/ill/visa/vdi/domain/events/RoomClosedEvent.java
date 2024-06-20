package eu.ill.visa.vdi.domain.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomClosedEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(RoomClosedEvent.class);

    public RoomClosedEvent() {
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Room closed (owner away) disconnect all clients");
        operations.sendEvent(ROOM_CLOSED_EVENT, client);
    }
}
