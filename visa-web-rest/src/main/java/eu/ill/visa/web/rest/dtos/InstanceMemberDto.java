package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

public class InstanceMemberDto implements Comparable<InstanceMemberDto> {

    private final Long id;
    private final UserDto user;
    private final InstanceMemberRole role;

    public InstanceMemberDto(final InstanceMember instanceMember) {
        this.id = instanceMember.getId();
        this.user = new UserDto(instanceMember.getUser());
        this.role = instanceMember.getRole();
    }

    public InstanceMemberDto(final UserDto user, final InstanceMemberRole role) {
        this.id = null;
        this.user = user;
        this.role = role;
    }

    public UserDto getUser() {
        return user;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public Long getId() {
        return id;
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
