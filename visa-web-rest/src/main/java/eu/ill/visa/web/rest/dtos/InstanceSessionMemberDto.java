package eu.ill.visa.web.rest.dtos;

import java.util.Date;

public class InstanceSessionMemberDto {

    private Long id;
    private Date createdAt;
    private InstanceSessionDto instanceSession;
    private String sessionId;
    private UserSimpleDto user;
    private String role;
    private boolean active;
    private Date lastSeenAt;


    public InstanceSessionMemberDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public InstanceSessionDto getInstanceSession() {
        return instanceSession;
    }

    public void setInstanceSession(InstanceSessionDto instanceSession) {
        this.instanceSession = instanceSession;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UserSimpleDto getUser() {
        return user;
    }

    public void setUser(UserSimpleDto user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
