package eu.ill.visa.cloud.domain;

public enum CloudInstanceState {
    UNKNOWN,
    BUILDING,
    STARTING,
    ACTIVE,
    ACTIVE_MIGRATING,
    STOPPING,
    STOPPED,
    REBOOTING,
    MIGRATING,
    UNAVAILABLE,
    ERROR,
    DELETED,
}
