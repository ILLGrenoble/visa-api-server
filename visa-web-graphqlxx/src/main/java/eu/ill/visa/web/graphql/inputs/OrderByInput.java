package eu.ill.visa.web.graphql.inputs;

import eu.ill.visa.core.domain.OrderBy;
import org.eclipse.microprofile.graphql.Input;

@Input("OrderBy")
public class OrderByInput {
    private String name;
    private Boolean ascending = true;

    public OrderByInput() {
    }

    public OrderByInput(String name, Boolean ascending) {
        this.name = name;
        this.ascending = ascending;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending;
    }

    public OrderBy toOrderBy() {
        return new OrderBy(this.name, this.ascending);
    }
}
