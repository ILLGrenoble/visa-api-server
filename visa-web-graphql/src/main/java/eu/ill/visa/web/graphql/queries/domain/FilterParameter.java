package eu.ill.visa.web.graphql.queries.domain;

public class FilterParameter {

    private String name;
    private String value;


    public FilterParameter() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
