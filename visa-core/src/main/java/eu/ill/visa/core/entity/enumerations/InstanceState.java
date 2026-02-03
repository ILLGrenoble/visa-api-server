package eu.ill.visa.core.entity.enumerations;

public enum InstanceState {
    ACTIVE(true),
    PARTIALLY_ACTIVE(true),
    ACTIVE_MIGRATING(true),
    BUILDING(false),
    DELETED(false),
    DELETING(false),
    ERROR(false),
    STARTING(false),
    STOPPED(false),
    STOPPING(false),
    REBOOTING(false),
    MIGRATING(false),
    UNAVAILABLE(false),
    UNKNOWN(false);

    private InstanceState(boolean isActive) {
        this.isActive = isActive;
    }

    private final boolean isActive;

    public boolean isActive() {
        return isActive;
    }
}
