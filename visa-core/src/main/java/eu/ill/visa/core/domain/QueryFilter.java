package eu.ill.visa.core.domain;


import java.util.ArrayList;
import java.util.List;

public class QueryFilter {

    private String query;
    private List<Parameter> parameters = new ArrayList<>();

    public QueryFilter() {

    }

    public QueryFilter(String query) {
        this.query = query;
    }

    public QueryFilter(String query, List<Parameter> parameters) {
        this.query = query;
        this.parameters = parameters;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public QueryFilter addParameter(String name, String value) {
        this.parameters.add(new Parameter(name, value));
        return this;
    }

}
