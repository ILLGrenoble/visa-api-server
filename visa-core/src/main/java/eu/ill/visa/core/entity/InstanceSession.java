package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceSession.getById", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN Instance i on isess.instanceId = i.id
        WHERE isess.id = :id
        AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSession.getAll", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN Instance i on isess.instanceId = i.id
        WHERE isess.current = true
        AND i.deletedAt IS NULL
        ORDER BY isess.id DESC
    """),
    @NamedQuery(name = "instanceSession.getAllByInstance", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN Instance i on isess.instanceId = i.id
        WHERE i = :instance
        AND isess.current = true
        AND i.deletedAt IS NULL
        ORDER BY isess.id DESC
    """),
    @NamedQuery(name = "instanceSession.getLastByInstance", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN Instance i on isess.instanceId = i.id
        WHERE i = :instance
        AND i.deletedAt IS NULL
        ORDER BY isess.id DESC
    """),
    @NamedQuery(name = "instanceSession.getAllByInstanceIdAndProtocol", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN Instance i on isess.instanceId = i.id
        WHERE i.id = :instanceId
        AND isess.protocol = :protocol
        AND isess.current = true
        AND i.deletedAt IS NULL
        ORDER BY isess.id DESC
    """),
    @NamedQuery(name = "instanceSession.updatePartialById", query = """
            UPDATE InstanceSession i
            SET i.current = :current
            WHERE i.id = :id
    """),
})
@Table(name = "instance_session")
public class InstanceSession extends Timestampable {

    public static final String GUACAMOLE_PROTOCOL = "guacamole";
    public static final String WEBX_PROTOCOL = "webx";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "connection_id", length = 150, nullable = false)
    private String connectionId;

    @Column(name = "protocol", length = 150, nullable = true)
    private String protocol;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(name = "current", nullable = false)
    private Boolean current;

    public InstanceSession() {
    }

    public InstanceSession(Long instanceId, String protocol, String connectionId) {
        this.instanceId = instanceId;
        this.protocol = protocol;
        this.connectionId = connectionId;
        this.current = true;
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

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceSession that = (InstanceSession) o;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("connectionId", connectionId)
            .append("current", current)
            .toString();
    }

    /**
     * Entity mapping used to force foreign key constraints onto instance_id. This entity is not used elsewhere.
     */
    @Entity
    @Table(name = "instance_session")
    private static class InstanceSessionInner {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
        private Instance instance;
    }
}
