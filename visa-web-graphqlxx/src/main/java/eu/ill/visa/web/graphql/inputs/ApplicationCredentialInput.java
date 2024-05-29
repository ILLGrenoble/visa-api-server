package eu.ill.visa.web.graphql.inputs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.graphql.Input;

@Input("ApplicationCredentialInput")
public class ApplicationCredentialInput {

    @Size(max = 250)
    private @NotNull String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
