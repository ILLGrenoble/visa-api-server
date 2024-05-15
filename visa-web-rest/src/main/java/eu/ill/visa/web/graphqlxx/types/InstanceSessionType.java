package eu.ill.visa.web.graphqlxx.types;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class InstanceSessionType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private String connectionId;
    private InstanceType instance;
    private boolean current;

    public InstanceSessionType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public InstanceType getInstance() {
        return instance;
    }

    public void setInstance(InstanceType instance) {
        this.instance = instance;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
