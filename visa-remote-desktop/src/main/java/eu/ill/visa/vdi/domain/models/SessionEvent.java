package eu.ill.visa.vdi.domain.models;

public interface SessionEvent {

    String EVENT_CHANNEL_OPEN = "event_channel_open";
    String USER_CONNECTED_EVENT = "user_connected";
    String USER_DISCONNECTED_EVENT = "user_disconnected";
    String USERS_CONNECTED_EVENT = "users_connected";
    String OWNER_AWAY_EVENT = "owner_away";
    String SESSION_LOCKED_EVENT = "session_locked";
    String SESSION_UNLOCKED_EVENT = "session_unlocked";
    String ACCESS_DENIED = "access_denied";
    String ACCESS_REQUEST_EVENT = "access_request";
    String ACCESS_REPLY_EVENT = "access_reply";
    String ACCESS_PENDING_EVENT = "access_pending";
    String ACCESS_GRANTED_EVENT = "access_granted";
    String ACCESS_CANCELLATION_EVENT = "access_cancel";
    String ACCESS_REVOKED_EVENT = "access_revoked";
}
