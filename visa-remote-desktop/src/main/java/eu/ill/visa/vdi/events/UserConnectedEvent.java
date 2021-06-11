package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.models.ConnectedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConnectedEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(UserConnectedEvent.class);

    private final ConnectedUser connectedUser;

    public UserConnectedEvent(final ConnectedUser connectedUser) {
        this.connectedUser = connectedUser;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("User connected: {}", connectedUser.getFullName() + " (" + connectedUser.getId() + ", " + connectedUser.getRole().toString() + ")");
        operations.sendEvent(USER_CONNECTED_EVENT, client, connectedUser);
    }
}
