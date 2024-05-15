package eu.ill.visa.web.graphqlxx.types;

import java.util.Date;

public class UserRoleType {

    private RoleType role;
    private Date expiresAt;

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
