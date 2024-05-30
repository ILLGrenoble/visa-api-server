package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.SecurityGroupFilter;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("SecurityGroupFilter")
public class SecurityGroupFilterType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long objectId;
    private final @NotNull String objectType;
    private final @NotNull SecurityGroupType securityGroup;

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
