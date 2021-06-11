package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.domain.AccessCancellation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessCancellationEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(AccessCancellationEvent.class);

    private String userFullName;

    public AccessCancellationEvent(String userFullName) {
        this.userFullName = userFullName;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Broadcasting access cancellation event");
        operations.sendEvent(ACCESS_CANCELLATION_EVENT, client, new AccessCancellation(userFullName, client.getSessionId().toString()));
    }
}
