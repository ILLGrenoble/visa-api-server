package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("UserRole")
public class UserRoleType {

    private final @NotNull RoleType role;
    private final Date expiresAt;

    public UserRoleType(final UserRole userRole) {
        this.role = new RoleType(userRole.getRole());
        this.expiresAt = userRole.getExpiresAt();
    }

    public RoleType getRole() {
        return role;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}
