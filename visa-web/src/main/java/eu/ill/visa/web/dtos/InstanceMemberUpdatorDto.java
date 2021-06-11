package eu.ill.visa.web.dtos;

import eu.ill.visa.core.domain.enumerations.InstanceMemberRole;

import javax.validation.constraints.NotNull;

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
