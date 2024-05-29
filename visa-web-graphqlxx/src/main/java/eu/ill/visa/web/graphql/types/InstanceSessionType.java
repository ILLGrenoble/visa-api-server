package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceSession;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("InstanceSession")
public class InstanceSessionType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String connectionId;
    private final InstanceType instance;
    private final boolean current;

    public InstanceSessionType(final InstanceSession session) {
        this.id = session.getId();
        this.connectionId = session.getConnectionId();
        this.instance = new InstanceType(session.getInstance());
        this.current = session.getCurrent();
    }

    public Long getId() {
        return id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public InstanceType getInstance() {
        return instance;
    }

    public boolean isCurrent() {
        return current;
    }
}
