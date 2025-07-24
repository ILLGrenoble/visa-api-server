package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import jakarta.validation.constraints.NotNull;

public class PersonalAccessTokenInputDto {

    private Long id;

    @NotNull
    private InstanceMemberRole role;

    @NotNull
    private String name;

    public PersonalAccessTokenInputDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
