package eu.ill.visa.web.bundles.graphql.queries.inputs;

public class SecurityGroupFilterInput {

    private Long securityGroupId;
    private Long objectId;
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
