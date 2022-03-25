package eu.ill.visa.web.bundles.graphql.queries.inputs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ApplicationCredentialInput {

    @NotNull
    @Size(max = 250)
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
