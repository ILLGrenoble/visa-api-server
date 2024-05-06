package eu.ill.visa.web.graphql.queries.domain;

public class CloudSecurityGroup {

    private String name;

    public CloudSecurityGroup() {

    }

    public CloudSecurityGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
