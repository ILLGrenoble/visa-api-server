package eu.ill.visa.web.graphql.types;

import org.eclipse.microprofile.graphql.Type;

@Type("CloudSecurityGroup")
public class CloudSecurityGroupType {

    private final String name;

    public CloudSecurityGroupType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
