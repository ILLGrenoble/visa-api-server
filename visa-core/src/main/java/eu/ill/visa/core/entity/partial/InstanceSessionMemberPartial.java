package eu.ill.visa.core.entity.partial;


import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;

@RegisterForReflection
public class InstanceSessionMemberPartial {
    private final Long id;
    private final InstanceMemberRole role;
    private Boolean active;
    private Date lastInteractionAt;
    private final Long instanceId;
    private final String userId;
    private final String userFirstName;
    private final String userLastName;

    public InstanceSessionMemberPartial(Long id, InstanceMemberRole role, Boolean active, Date lastInteractionAt, Long instanceId, String userId, String userFirstName, String userLastName) {
        this.userId = userId;
        this.id = id;
        this.role = role;
        this.active = active;
        this.lastInteractionAt = lastInteractionAt;
        this.instanceId = instanceId;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
    }

    public Long getId() {
        return id;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public String getUserFullName() {
        return userFirstName + " " + userLastName;
    }
}
