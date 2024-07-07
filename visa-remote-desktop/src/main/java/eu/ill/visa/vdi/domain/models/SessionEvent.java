package eu.ill.visa.vdi.domain.models;

public interface SessionEvent {

    String EVENT_CHANNEL_OPEN = "event_channel_open";
    String USER_CONNECTED_EVENT = "user:connected";
    String USER_DISCONNECTED_EVENT = "user:disconnected";
    String USERS_CONNECTED_EVENT = "users:connected";
    String OWNER_AWAY_EVENT = "owner:away";
    String SESSION_LOCKED_EVENT = "session:locked";
    String SESSION_UNLOCKED_EVENT = "session:unlocked";
    String ACCESS_DENIED = "access:denied";
    String ACCESS_REQUEST_EVENT = "access:request";
    String ACCESS_REPLY_EVENT = "access:reply";
    String ACCESS_PENDING_EVENT = "access:pending";
    String ACCESS_GRANTED_EVENT = "access:granted";
    String ACCESS_CANCELLATION_EVENT = "access:cancel";
    String ACCESS_REVOKED_EVENT = "access:revoked";
}
