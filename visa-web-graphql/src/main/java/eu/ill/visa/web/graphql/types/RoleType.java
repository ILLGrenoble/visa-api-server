package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Role;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("Role")
public class RoleType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final String description;
    private final Date groupCreatedAt;

    public RoleType(final Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.groupCreatedAt = role.getGroupCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getGroupCreatedAt() {
        return groupCreatedAt;
    }
}
