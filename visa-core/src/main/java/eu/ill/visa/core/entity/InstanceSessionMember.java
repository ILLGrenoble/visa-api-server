package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "instance_session_member")
public class InstanceSessionMember extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_session_id", foreignKey = @ForeignKey(name = "fk_instance_session_id"), nullable = false)
    private InstanceSession instanceSession;

    @Column(name = "connection_id", length = 150, nullable = false)
    private String sessionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User user;

    @Column(name = "role", length = 150, nullable = false)
    private String role;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "last_seen_at", nullable = true)
    private Date lastSeenAt = new Date();

    @Column(name = "last_interaction_at", nullable = true)
    private Date lastInteractionAt = new Date();


    public InstanceSessionMember() {
    }

    public InstanceSessionMember(InstanceSession instanceSession, String sessionId, User user, String role) {
        this.instanceSession = instanceSession;
        this.sessionId = sessionId;
        this.user = user;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InstanceSession getInstanceSession() {
        return instanceSession;
    }

    public void setInstanceSession(InstanceSession instanceSession) {
        this.instanceSession = instanceSession;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public void updateLastSeenAt() {
        this.lastSeenAt = new Date();
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }
}
