package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceSession;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("InstanceSession")
public class InstanceSessionType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String connectionId;
    private final @NotNull boolean current;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long instanceId;

    public InstanceSessionType(final InstanceSession session) {
        this.id = session.getId();
        this.connectionId = session.getConnectionId();
        this.current = session.getCurrent();
        this.instanceId = session.getInstanceId();
    }

    public Long getId() {
        return id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public boolean isCurrent() {
        return current;
    }

    public @NotNull Long getInstanceId() {
        return instanceId;
    }
}
