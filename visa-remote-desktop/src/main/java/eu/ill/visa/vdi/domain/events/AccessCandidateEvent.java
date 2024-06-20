package eu.ill.visa.vdi.domain.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.domain.models.AccessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessCandidateEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(AccessCandidateEvent.class);

    private final String userFullName;

    public AccessCandidateEvent(final String userFullName) {
        this.userFullName = userFullName;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Broadcasting access candidate event");
        operations.sendEvent(ACCESS_CANDIDATE_EVENT, client, new AccessRequest(this.userFullName, client.getSessionId().toString()));
    }
}
