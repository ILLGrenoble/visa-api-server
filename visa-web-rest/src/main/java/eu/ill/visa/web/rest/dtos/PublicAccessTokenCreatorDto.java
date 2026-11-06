package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import jakarta.validation.constraints.NotNull;

public class PublicAccessTokenCreatorDto {

    @NotNull
    private InstanceMemberRole role;

    public PublicAccessTokenCreatorDto() {
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }
}
