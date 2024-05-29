package eu.ill.visa.web.graphql.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.visa.core.entity.SecurityGroup;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class SecurityGroupType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
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

    @JsonIgnore
    public Long getCloudId() {
        return cloudId;
    }
}
