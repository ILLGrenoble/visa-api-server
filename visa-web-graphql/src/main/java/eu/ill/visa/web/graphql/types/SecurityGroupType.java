package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.SecurityGroup;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("SecurityGroup")
public class SecurityGroupType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    @AdaptToScalar(Scalar.Int.class)
    private final Long cloudId;

    public SecurityGroupType(final SecurityGroup securityGroup) {
        this.id = securityGroup.getId();
        this.name = securityGroup.getName();
        this.cloudId = securityGroup.getCloudId();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCloudId() {
        return cloudId;
    }
}
