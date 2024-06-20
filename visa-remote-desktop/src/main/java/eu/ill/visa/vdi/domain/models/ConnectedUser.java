package eu.ill.visa.vdi.domain.models;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class ConnectedUser {

    private String id;
    private String fullName;
    private Role role;

    public ConnectedUser() {
    }

    public ConnectedUser(String id, String fullName, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isRole(Role role) {
        return this.role == role;
    }

    public boolean hasAnyRole(List<Role> targetRoles) {
        for (final Role targetRole : targetRoles) {
            if (this.isRole(targetRole)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("fullName", fullName)
            .append("role", role)
            .toString();
    }
}
