package eu.ill.visa.web.dtos;

import eu.ill.visa.core.domain.enumerations.InstanceMemberRole;

public class InstanceMemberDto implements Comparable<InstanceMemberDto> {

    private Long id;

    private UserSimpleDto user;

    private InstanceMemberRole role;

    public InstanceMemberDto() {
    }

    public InstanceMemberDto(UserSimpleDto user, InstanceMemberRole role) {
        this.user = user;
        this.role = role;
    }

    public UserSimpleDto getUser() {
        return user;
    }

    public void setUser(UserSimpleDto user) {
        this.user = user;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int compareTo(InstanceMemberDto member) {
        // Owner always comes first
        if (member.role == InstanceMemberRole.OWNER) {
            return 1;
        }
        return this.user.getLastName().compareTo(member.user.getLastName());
    }

}
