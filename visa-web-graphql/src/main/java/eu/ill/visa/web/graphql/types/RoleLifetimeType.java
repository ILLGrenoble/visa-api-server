package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.FlavourRoleLifetime;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("RoleLifetime")
public class RoleLifetimeType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final RoleType role;
    private final @NotNull @AdaptToScalar(Scalar.Int.class) Long lifetimeMinutes;

    public RoleLifetimeType(final FlavourRoleLifetime flavourRoleLifetime) {
        this.id = flavourRoleLifetime.getId();
        this.role = new RoleType(flavourRoleLifetime.getRole());
        this.lifetimeMinutes = flavourRoleLifetime.getDuration().getDuration().toMinutes();
    }

    public Long getId() {
        return id;
    }

    public RoleType getRole() {
        return role;
    }

    public Long getLifetimeMinutes() {
        return lifetimeMinutes;
    }
}
