package eu.ill.visa.web.graphql.inputs;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

@Input("RoleInput")
public class RoleInput {

    private @NotNull String name;
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
