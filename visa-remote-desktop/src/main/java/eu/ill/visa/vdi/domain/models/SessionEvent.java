package eu.ill.visa.vdi.domain.models;

public interface SessionEvent {

    String USER_CONNECTED_EVENT = "vdi:user_connected";
    String USER_DISCONNECTED_EVENT = "vdi:user_disconnected";
    String USERS_CONNECTED_EVENT = "vdi:users_connected";
    String OWNER_AWAY_EVENT = "vdi:owner_away";
    String SESSION_LOCKED_EVENT = "vdi:session_locked";
    String SESSION_UNLOCKED_EVENT = "vdi:session_unlocked";
    String ACCESS_DENIED = "vdi:access_denied";
    String ACCESS_REQUEST_EVENT = "vdi:access_request";
    String ACCESS_REPLY_EVENT = "vdi:access_reply";
    String ACCESS_PENDING_EVENT = "vdi:access_pending";
    String ACCESS_GRANTED_EVENT = "vdi:access_granted";
    String ACCESS_CANCELLATION_EVENT = "vdi:access_cancel";
    String ACCESS_REVOKED_EVENT = "vdi:access_revoked";
}
