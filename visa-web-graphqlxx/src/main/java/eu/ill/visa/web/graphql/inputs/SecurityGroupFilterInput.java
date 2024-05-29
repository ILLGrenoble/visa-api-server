package eu.ill.visa.web.graphql.inputs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.graphql.Input;

@Input("SecurityGroupFilterInput")
public class SecurityGroupFilterInput {

    private @NotNull Long securityGroupId;
    private @NotNull Long objectId;
    @Pattern(regexp = "INSTRUMENT|ROLE|FLAVOUR")
    private @NotNull String objectType;

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
