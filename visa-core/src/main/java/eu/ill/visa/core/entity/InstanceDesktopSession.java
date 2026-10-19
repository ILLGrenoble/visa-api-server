package eu.ill.visa.core.entity;

import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceDesktopSession.getById", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.id = :id
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceDesktopSession.getAll", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.countAll", query = """
        SELECT count(ids.id)
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.closedAt IS NULL
        AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceDesktopSession.countAllActive", query = """
        SELECT count(ids.id)
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.closedAt IS NULL
        AND ids.lastInteractionAt > :timeAgo
        AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllByInstanceId", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.instanceId = :instanceId
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllByUserId", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.userId = :userId
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllByInstanceIds", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.instanceId IN :instanceIds
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllHistoricalByInstanceId", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.instanceId = :instanceId
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllByConnectionId", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.connectionId = :connectionId
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllWithConnectedUserByConnectionId", query = """
        SELECT ids, new eu.ill.visa.common.vdi.domain.ConnectedUser(u.id, u.firstName, u.lastName, ids.role)
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        LEFT JOIN User u on ids.userId = u.id
        WHERE ids.connectionId = :connectionId
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllConnectedUsersByConnectionId", query = """
        SELECT new eu.ill.visa.common.vdi.domain.ConnectedUser(u.id, u.firstName, u.lastName, ids.role)
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        LEFT JOIN User u on ids.userId = u.id
        WHERE ids.connectionId = :connectionId
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY u.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getAllConnectedUsersByInstanceId", query = """
        SELECT new eu.ill.visa.common.vdi.domain.ConnectedUser(u.id, u.firstName, u.lastName, ids.role)
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        LEFT JOIN User u on ids.userId = u.id
        WHERE ids.instanceId = :instanceId
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY u.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.getByConnectionIdAndClientId", query = """
        SELECT ids
        FROM InstanceDesktopSession ids
        LEFT JOIN Instance i on ids.instanceId = i.id
        WHERE ids.connectionId = :connectionId
        AND ids.clientId = :clientId
        AND ids.closedAt IS NULL
        AND i.deletedAt IS NULL
        ORDER BY ids.id DESC
    """),
    @NamedQuery(name = "instanceDesktopSession.closeAll", query = """
            UPDATE InstanceDesktopSession ids
            SET ids.closedAt = CURRENT_TIMESTAMP, ids.updatedAt = CURRENT_TIMESTAMP
            WHERE ids.closedAt IS NULL
    """),
    @NamedQuery(name = "instanceDesktopSession.cleanup", query = """
            UPDATE InstanceDesktopSession ids
            SET ids.closedAt = ids.updatedAt
            WHERE ids.closedAt IS NULL
    """),
    @NamedQuery(name = "instanceDesktopSession.closeAllByInstanceIdAndConnectionIdAndClientId", query = """
        UPDATE InstanceDesktopSession ids
        SET ids.closedAt = CURRENT_TIMESTAMP, ids.updatedAt = CURRENT_TIMESTAMP
        WHERE ids.instanceId = :instanceId
        AND ids.connectionId = :connectionId
        AND ids.clientId = :clientId
        AND ids.closedAt IS NULL
    """),
    @NamedQuery(name = "instanceDesktopSession.closeById", query = """
            UPDATE InstanceDesktopSession ids
            SET ids.closedAt = CURRENT_TIMESTAMP, ids.updatedAt = CURRENT_TIMESTAMP
            WHERE ids.id = :id
            AND ids.closedAt IS NULL
    """),
    @NamedQuery(name = "instanceDesktopSession.updateClientPingById", query = """
            UPDATE InstanceDesktopSession ids
            SET ids.lastClientPingAt = :lastClientPingAt, ids.updatedAt = CURRENT_TIMESTAMP
            WHERE ids.id = :id
    """),
    @NamedQuery(name = "instanceDesktopSession.updateInteractionAtById", query = """
            UPDATE InstanceDesktopSession ids
            SET ids.lastInteractionAt = :lastInteractionAt, ids.updatedAt = CURRENT_TIMESTAMP
            WHERE ids.id = :id
    """),
})
@Table(name = "instance_desktop_session")
public class InstanceDesktopSession extends Timestampable {

    public static final String GUACAMOLE_PROTOCOL = "guacamole";
    public static final String WEBX_PROTOCOL = "webx";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "connection_id", length = 150, nullable = false)
    private String connectionId;

    @Column(name = "client_id", length = 150, nullable = false)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 150, nullable = false)
    private InstanceMemberRole role;

    @Column(name = "protocol", length = 150, nullable = true)
    private String protocol;

    @Column(name = "last_interaction_at", nullable = true)
    private Date lastInteractionAt = new Date();

    @Column(name = "last_client_ping_at", nullable = true)
    private Date lastClientPingAt = new Date();

    @Column(name = "closed_at", nullable = true)
    private Date closedAt;

    public InstanceDesktopSession() {
    }

    public InstanceDesktopSession(final Long instanceId,
                                  final String userId,
                                  final String connectionId,
                                  final String clientId,
                                  final InstanceMemberRole role,
                                  final String protocol) {
        this.instanceId = instanceId;
        this.userId = userId;
        this.connectionId = connectionId;
        this.clientId = clientId;
        this.role = role;
        this.protocol = protocol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public Date getLastClientPingAt() {
        return lastClientPingAt;
    }

    public void setLastClientPingAt(Date lastClientPingAt) {
        this.lastClientPingAt = lastClientPingAt;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceDesktopSession that = (InstanceDesktopSession) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }


    /**
     * Entity mapping used to force foreign key constraints onto instance_id and user_id. This entity is not used elsewhere.
     * This allows us to optimise the requests to get InstanceDesktopSessions without having to map to an
     * Instance and User object.
     */
    @Entity
    @Table(name = "instance_desktop_session")
    private static class InstanceDesktopSessionInner {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
        private Instance instance;

        @ManyToOne
        @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_id"), nullable = false)
        private User user;
    }
}
