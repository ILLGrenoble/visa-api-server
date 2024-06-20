package eu.ill.visa.vdi.domain.events;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;

public abstract class Event {

    public static final String USER_CONNECTED_EVENT = "user:connected";
    public static final String USER_DISCONNECTED_EVENT = "user:disconnected";
    public static final String USERS_CONNECTED_EVENT = "users:connected";
    public static final String OWNER_AWAY_EVENT = "owner:away";
    public static final String ROOM_CLOSED_EVENT = "room:closed";
    public static final String ROOM_LOCKED_EVENT = "room:locked";
    public static final String ROOM_UNLOCKED_EVENT = "room:unlocked";
    public static final String ACCESS_DENIED = "access:denied";
    public static final String ACCESS_CANDIDATE_EVENT = "access:candidate";
    public static final String ACCESS_REQUEST_EVENT = "access:request";
    public static final String ACCESS_REPLY_EVENT = "access:reply";
    public static final String ACCESS_PENDING_EVENT = "access:pending";
    public static final String ACCESS_GRANTED_EVENT = "access:granted";
    public static final String ACCESS_CANCELLATION_EVENT = "access:cancel";
    public static final String ACCESS_REVOKED_EVENT = "access:revoked";

    public abstract void broadcast(final SocketIOClient client, final BroadcastOperations operations);
}
