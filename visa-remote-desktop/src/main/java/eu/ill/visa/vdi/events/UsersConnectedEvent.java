package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.vdi.models.ConnectedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UsersConnectedEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(UsersConnectedEvent.class);

    private final Instance instance;
    private final List<ConnectedUser> users;

    public UsersConnectedEvent(final Instance instance, final List<ConnectedUser> users) {
        this.instance = instance;
        this.users = users;
    }

    @Override
    public void broadcast(SocketIOClient client, BroadcastOperations operations) {
        if (!users.isEmpty()) {
            logger.info("Instance {} has the following users connected: {}", instance.getId(), users.stream().map(user ->
                user.getFullName() + " (" + user.getId() + ", " + user.getRole().toString() + ")").toList());
        }
        operations.sendEvent(USERS_CONNECTED_EVENT, users);
    }
}
