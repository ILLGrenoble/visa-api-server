package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

import java.util.List;
import java.util.Objects;

public class ConnectedUser {

    private String id;
    private String fullName;
    private InstanceMemberRole role;

    public ConnectedUser() {
    }

    public ConnectedUser(String id, String fullName, InstanceMemberRole role) {
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

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    public boolean isRole(InstanceMemberRole role) {
        return this.role == role;
    }

    public boolean hasAnyRole(List<InstanceMemberRole> targetRoles) {
        for (final InstanceMemberRole targetRole : targetRoles) {
            if (this.isRole(targetRole)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectedUser that = (ConnectedUser) o;
        return Objects.equals(id, that.id) && Objects.equals(fullName, that.fullName) && role == that.role;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(fullName);
        result = 31 * result + Objects.hashCode(role);
        return result;
    }

    @Override
    public String toString() {
        return this.fullName + " (" + this.id + ", " + this.role + ")";
    }
}
