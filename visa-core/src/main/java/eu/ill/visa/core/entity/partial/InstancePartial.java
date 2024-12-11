package eu.ill.visa.core.entity.partial;


import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;

@RegisterForReflection
public class InstancePartial {
    private Long id;
    private Date lastSeenAt;
    private Date lastInteractionAt;

    public InstancePartial(Long id, Date lastSeenAt, Date lastInteractionAt) {
        this.id = id;
        this.lastSeenAt = lastSeenAt;
        this.lastInteractionAt = lastInteractionAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public void updateLastSeenAt() {
        this.lastSeenAt = new Date();
    }

    public void updateLastInteractionAt() {
        this.lastInteractionAt = new Date();
    }
}
