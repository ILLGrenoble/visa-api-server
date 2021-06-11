package eu.ill.visa.web.bundles.graphql.queries.inputs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateRoleInput {

    @NotNull
    @Size(min = 1, max = 100)

    private String name;

    @Size(max = 250)
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
