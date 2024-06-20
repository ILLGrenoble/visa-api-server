package eu.ill.visa.vdi.domain.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.domain.models.AccessRequest;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessCandidateEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(AccessCandidateEvent.class);

    private final ConnectedUser user;

    public AccessCandidateEvent(final ConnectedUser user) {
        this.user = user;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Broadcasting access candidate event");
        operations.sendEvent(ACCESS_CANDIDATE_EVENT, client, new AccessRequest(this.user, client.getSessionId().toString()));
    }
}
