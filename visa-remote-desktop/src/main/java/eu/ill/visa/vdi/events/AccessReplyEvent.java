package eu.ill.visa.vdi.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.domain.AccessReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessReplyEvent extends Event {

    private final static Logger logger = LoggerFactory.getLogger(AccessReplyEvent.class);

    private AccessReply accessReply;

    public AccessReplyEvent(AccessReply accessReply) {
        this.accessReply = accessReply;
    }

    @Override
    public void broadcast(final SocketIOClient client, final BroadcastOperations operations) {
        logger.info("Broadcasting access candidate reply for client {}", this.accessReply.getId());
        operations.sendEvent(ACCESS_REPLY_EVENT, client, this.accessReply);
    }
}
