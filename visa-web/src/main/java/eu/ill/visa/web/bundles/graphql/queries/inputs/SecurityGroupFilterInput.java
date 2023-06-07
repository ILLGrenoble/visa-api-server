package eu.ill.visa.web.bundles.graphql.queries.inputs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class SecurityGroupFilterInput {

    @NotNull
    private Long securityGroupId;

    @NotNull
    private Long objectId;

    @NotNull
    @Pattern(regexp = "INSTRUMENT|ROLE|FLAVOUR")
    private String objectType;

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
}
