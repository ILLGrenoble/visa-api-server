package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.enumerations.InstanceState;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class InstanceStateCount {

    private final InstanceState state;
    @AdaptToScalar(Scalar.Int.class)
    private final Long count;

    public InstanceStateCount(InstanceState state, Long count) {
        this.state = state;
        this.count = count;
    }

    public InstanceState getState() {
        return state;
    }

    public Long getCount() {
        return count;
    }
}
