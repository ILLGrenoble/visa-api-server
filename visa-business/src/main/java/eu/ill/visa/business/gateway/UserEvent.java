package eu.ill.visa.business.gateway;

public interface UserEvent {
    String INSTANCE_STATE_CHANGED_EVENT = "user:instance_state_changed";
    String INSTANCES_CHANGED_EVENT = "user:instances_changed";
    String INSTANCE_THUMBNAIL_UPDATED_EVENT = "user:instance_thumbnail_updated";
}
