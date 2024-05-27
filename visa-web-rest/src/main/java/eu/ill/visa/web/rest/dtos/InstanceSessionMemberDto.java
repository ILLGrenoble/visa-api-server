package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceSessionMember;

import java.util.Date;

public class InstanceSessionMemberDto {

    private final Long id;
    private final Date createdAt;
    private final InstanceSessionDto instanceSession;
    private final String sessionId;
    private final UserDto user;
    private final String role;
    private final boolean active;
    private final Date lastSeenAt;


    public InstanceSessionMemberDto(final InstanceSessionMember instanceSessionMember) {
        this.id = instanceSessionMember.getId();
        this.createdAt = instanceSessionMember.getCreatedAt();
        this.instanceSession = new InstanceSessionDto(instanceSessionMember.getInstanceSession());
        this.sessionId = instanceSessionMember.getSessionId();
        this.user = new UserDto(instanceSessionMember.getUser());
        this.role = instanceSessionMember.getRole();
        this.active = instanceSessionMember.isActive();
        this.lastSeenAt = instanceSessionMember.getLastSeenAt();
    }

    public Long getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public InstanceSessionDto getInstanceSession() {
        return instanceSession;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserDto getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }
}
