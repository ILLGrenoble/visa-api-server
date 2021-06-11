package eu.ill.visa.cloud.domain;

public enum CloudInstanceState {
    UNKNOWN,
    BUILDING,
    STARTING,
    ACTIVE,
    STOPPING,
    STOPPED,
    REBOOTING,
    UNAVAILABLE,
    ERROR,
    DELETED,
}
