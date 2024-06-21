package eu.ill.visa.vdi.domain.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConnectedEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(UserConnectedEvent.class);
    private record UserConnectedData(ConnectedUser user, String connectionId) {}

    private final ConnectedUser connectedUser;
    private final String connectionId;

    public UserConnectedEvent(final ConnectedUser connectedUser, final String connectionId) {
        this.connectedUser = connectedUser;
        this.connectionId = connectionId;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("User connected: {}", connectedUser.getFullName() + " (" + connectedUser.getId() + ", " + connectedUser.getRole().toString() + ")");
        operations.sendEvent(USER_CONNECTED_EVENT, client, new UserConnectedData(connectedUser, connectionId));
    }

}