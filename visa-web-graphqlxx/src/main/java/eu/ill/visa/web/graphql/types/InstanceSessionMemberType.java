package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceSessionMember;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("InstanceSessionMember")
public class InstanceSessionMemberType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final Date createdAt;
    private final Date updatedAt;
    private final Date lastInteractionAt;
    private final InstanceSessionType instanceSession;
    private final String sessionId;
    private final UserType user;
    private final String role;
    private final Boolean active;

    public InstanceSessionMemberType(final InstanceSessionMember member) {
        this.id = member.getId();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();
        this.lastInteractionAt = member.getLastInteractionAt();
        this.instanceSession = member.getInstanceSession() == null ? null : new InstanceSessionType(member.getInstanceSession());
        this.sessionId = member.getSessionId();
        this.user = member.getUser() == null ? null : new UserType(member.getUser());
        this.role = member.getRole();
        this.active = member.isActive();
    }

    public Long getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public InstanceSessionType getInstanceSession() {
        return instanceSession;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserType getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }

    @AdaptToScalar(Scalar.Int.class)
    public Long getDuration() {
        final long now = new Date().getTime();
        final long createdAt = this.getCreatedAt().getTime();
        final long updatedAt = this.getUpdatedAt().getTime();
        if (this.getActive()) {
            return (now - createdAt) / 1000;
        }
        return ((updatedAt - createdAt) / 1000);
    }
}
