package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceSessionMember.getAll", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN instanceSession.instance instance
            WHERE i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getByInstanceSessionAndSessionId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN instanceSession.instance instance
            WHERE instanceSession = :instanceSession
            AND i.sessionId = :sessionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllByInstanceSessionId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN instanceSession.instance instance
            WHERE i.instanceSession.id = :instanceSessionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllForInstanceId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN instanceSession.instance instance
            WHERE instance.id = :instanceId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllByConnectionId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN instanceSession.instance instance
            WHERE instanceSession.connectionId = :connectionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllHistoryForInstanceId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN instanceSession.instance instance
            WHERE instance.id = :instanceId
            AND instance.deletedAt IS NULL
            ORDER BY i.id DESC
    """),
    @NamedQuery(name = "instanceSessionMember.getBySessionId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN instanceSession.instance instance
            WHERE i.sessionId = :sessionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
})
@Table(name = "instance_session_member")
public class InstanceSessionMember extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_session_id", foreignKey = @ForeignKey(name = "fk_instance_session_id"), nullable = false)
    private InstanceSession instanceSession;

    @Column(name = "session_id", length = 150, nullable = false)
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
