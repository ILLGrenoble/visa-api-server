package eu.ill.visa.web.graphql.types;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("CloudSecurityGroup")
public class CloudSecurityGroupType {

    private final @NotNull String name;

    public CloudSecurityGroupType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
