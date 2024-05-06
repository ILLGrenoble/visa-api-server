package eu.ill.visa.web.graphql.queries.inputs;

import jakarta.validation.constraints.NotNull;

public class RoleInput {

    @NotNull
    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
