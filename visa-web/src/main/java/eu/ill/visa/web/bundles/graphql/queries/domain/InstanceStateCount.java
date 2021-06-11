package eu.ill.visa.web.bundles.graphql.queries.domain;

import eu.ill.visa.core.domain.enumerations.InstanceState;

public class InstanceStateCount {

    private final InstanceState state;

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
