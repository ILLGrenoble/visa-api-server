package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceJupyterSession;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("InstanceJupyterSession")
public class InstanceJupyterSessionType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull InstanceType instance;
    private final @NotNull UserType user;
    private final @NotNull String kernelId;
    private final @NotNull String sessionId;
    private final Boolean active;
    private final @NotNull Date createdAt;
    private final Date updatedAt;

    public InstanceJupyterSessionType(final InstanceJupyterSession session) {
        this.id = session.getId();
        this.instance = session.getInstance() == null ? null : new InstanceType(session.getInstance());
        this.user = new UserType(session.getUser());
        this.kernelId = session.getKernelId();
        this.sessionId = session.getSessionId();
        this.active = session.isActive();
        this.createdAt = session.getCreatedAt();
        this.updatedAt = session.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public InstanceType getInstance() {
        return instance;
    }

    public UserType getUser() {
        return user;
    }

    public String getKernelId() {
        return kernelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Boolean getActive() {
        return active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    @AdaptToScalar(Scalar.Int.class)
    public Long duration() {
        final long now = new Date().getTime();
        final long createdAt = this.getCreatedAt().getTime();
        final long updatedAt = this.getUpdatedAt().getTime();
        if (this.getActive()) {
            return (now - createdAt) / 1000;
        }
        return ((updatedAt - createdAt) / 1000);
    }

}
