package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

import java.util.List;

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
    public String toString() {
        return this.fullName + " (" + this.id + ", " + this.role + ")";
    }
}
