package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

import jakarta.validation.constraints.NotNull;

public class InstanceMemberUpdatorDto {

    @NotNull
    private InstanceMemberRole role;


    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    public boolean isRole(InstanceMemberRole role) {
        return this.role == role;
    }
}
