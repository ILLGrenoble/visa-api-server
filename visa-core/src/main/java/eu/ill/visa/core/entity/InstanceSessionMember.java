package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceSessionMember.getAll", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.countAll", query = """
            SELECT count(i) FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.countAllActive", query = """
            SELECT count(i) FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE i.active = true
            AND i.lastInteractionAt > :timeAgo
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllByInstanceSessionId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN FETCH i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE i.instanceSession.id = :instanceSessionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllForInstanceId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN FETCH i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE instance.id = :instanceId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllForInstanceIds", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE instance.id IN :instanceIds
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllByConnectionId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN FETCH i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE instanceSession.connectionId = :connectionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllHistoryForInstanceId", query = """
            SELECT i FROM InstanceSessionMember i
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE instance.id = :instanceId
            AND instance.deletedAt IS NULL
            ORDER BY i.id DESC
    """),

    @NamedQuery(name = "instanceSessionMember.getAllPartials", query = """
            SELECT new eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial(i.id, i.role, i.active, i.lastInteractionAt, instance.id, u.id, u.firstName, u.lastName)
            FROM InstanceSessionMember i
            LEFT JOIN i.user u
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllPartialsForInstanceId", query = """
            SELECT new eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial(i.id, i.role, i.active, i.lastInteractionAt, instance.id, u.id, u.firstName, u.lastName)
            FROM InstanceSessionMember i
            LEFT JOIN i.user u
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE instance.id = :instanceId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getPartialByInstanceSessionIdAndSessionId", query = """
            SELECT new eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial(i.id, i.role, i.active, i.lastInteractionAt, instance.id, u.id, u.firstName, u.lastName)
            FROM InstanceSessionMember i
            LEFT JOIN i.user u
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE instanceSession.id = :instanceSessionId
            AND i.sessionId = :sessionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.getAllPartialByInstanceSessionId", query = """
            SELECT new eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial(i.id, i.role, i.active, i.lastInteractionAt, instance.id, u.id, u.firstName, u.lastName)
            FROM InstanceSessionMember i
            LEFT JOIN i.user u
            LEFT JOIN i.instanceSession instanceSession
            LEFT JOIN Instance instance ON instanceSession.instanceId = instance.id
            WHERE instanceSession.id = :instanceSessionId
            AND i.active = true
            AND instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSessionMember.updatePartialById", query = """
            UPDATE InstanceSessionMember i
            SET i.active = :active, i.lastInteractionAt = :lastInteractionAt
            WHERE i.id = :id
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 150, nullable = false)
    private InstanceMemberRole role;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @Column(name = "last_interaction_at", nullable = true)
    private Date lastInteractionAt = new Date();


    public InstanceSessionMember() {
    }

    public InstanceSessionMember(InstanceSession instanceSession, String sessionId, User user, InstanceMemberRole role) {
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

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }
}
