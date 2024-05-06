package eu.ill.visa.web.graphql.queries.inputs;

import eu.ill.visa.web.graphql.queries.domain.FilterParameter;

import java.util.List;

public class InstanceFilterInput {

    private String query;
    private List<FilterParameter> parameters;

    public InstanceFilterInput() {

    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<FilterParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<FilterParameter> parameters) {
        this.parameters = parameters;
    }
}
