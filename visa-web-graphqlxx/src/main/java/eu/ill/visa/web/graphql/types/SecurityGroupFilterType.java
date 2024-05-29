package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.SecurityGroupFilter;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class SecurityGroupFilterType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    @AdaptToScalar(Scalar.Int.class)
    private final Long objectId;
    private final String objectType;
    private final SecurityGroupType securityGroup;

    public SecurityGroupFilterType(final SecurityGroupFilter filter) {
        this.id = filter.getId();
        this.objectId = filter.getObjectId();
        this.objectType = filter.getObjectType();
        this.securityGroup = new SecurityGroupType(filter.getSecurityGroup());
    }

    public Long getId() {
        return id;
    }

    public Long getObjectId() {
        return objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public SecurityGroupType getSecurityGroup() {
        return securityGroup;
    }
}
