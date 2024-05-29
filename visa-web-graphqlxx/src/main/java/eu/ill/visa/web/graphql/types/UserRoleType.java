package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.UserRole;

import java.util.Date;

public class UserRoleType {

    private final RoleType role;
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
