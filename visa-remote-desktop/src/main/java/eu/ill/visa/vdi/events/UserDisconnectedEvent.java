package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.models.ConnectedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDisconnectedEvent extends Event {

    private static final Logger logger = LoggerFactory.getLogger(UserDisconnectedEvent.class);

    private final ConnectedUser connectedUser;

    public UserDisconnectedEvent(final ConnectedUser connectedUser) {
        this.connectedUser = connectedUser;
    }

    @Override
    public void broadcast(SocketIOClient client, BroadcastOperations operations) {
        logger.info("Disconnected: {}", connectedUser.getFullName());
        operations.sendEvent(USER_DISCONNECTED_EVENT, connectedUser);
    }
}
