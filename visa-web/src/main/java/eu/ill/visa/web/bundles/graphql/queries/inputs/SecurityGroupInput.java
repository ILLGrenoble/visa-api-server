package eu.ill.visa.web.bundles.graphql.queries.inputs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SecurityGroupInput {

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    private Long cloudId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

}
