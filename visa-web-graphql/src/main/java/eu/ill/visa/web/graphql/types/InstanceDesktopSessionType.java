package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceDesktopSession;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("InstanceDesktopSession")
public class InstanceDesktopSessionType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long instanceId;

    private final @NotNull String userId;

    private final @NotNull Date createdAt;
    private final Date updatedAt;
    private final Date lastClientPingAt;
    private final Date lastInteractionAt;
    private final Date closedAt;
    private final @NotNull String connectionId;
    private final @NotNull String clientId;
    private final String protocol;
    private final @NotNull InstanceMemberRole role;

    public InstanceDesktopSessionType(final InstanceDesktopSession session) {
        this.id = session.getId();
        this.instanceId = session.getInstanceId();
        this.userId = session.getUserId();
        this.createdAt = session.getCreatedAt();
        this.updatedAt = session.getUpdatedAt();
        this.lastClientPingAt = session.getLastClientPingAt();
        this.lastInteractionAt = session.getLastInteractionAt();
        this.closedAt = session.getClosedAt();
        this.connectionId = session.getConnectionId();
        this.clientId = session.getClientId();
        this.protocol = session.getProtocol();
        this.role = session.getRole();
    }

    public Long getId() {
        return id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public String getUserId() {
        return userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getLastClientPingAt() {
        return lastClientPingAt;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getProtocol() {
        return protocol;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    @AdaptToScalar(Scalar.Int.class)
    public @NotNull Long getDuration() {
        final long now = new Date().getTime();
        final long createdAt = this.getCreatedAt().getTime();
        if (this.getClosedAt() == null) {
            return (now - createdAt) / 1000;
        }
        final long closedAt = this.getClosedAt().getTime();
        return ((closedAt - createdAt) / 1000);
    }
}
