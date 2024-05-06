package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

import jakarta.validation.constraints.NotNull;

public class InstanceMemberCreatorDto {

    private String userId;

    @NotNull
    private InstanceMemberRole role;

    public InstanceMemberCreatorDto() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }
}
