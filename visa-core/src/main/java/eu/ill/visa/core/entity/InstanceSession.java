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
        LEFT JOIN isess.instance i
        WHERE isess.id = :id
        AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceSession.getAll", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN isess.instance i
        WHERE isess.current = true
        AND i.deletedAt IS NULL
        ORDER BY isess.id DESC
    """),
    @NamedQuery(name = "instanceSession.getAllByInstance", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN isess.instance i
        WHERE i = :instance
        AND isess.current = true
        AND i.deletedAt IS NULL
        ORDER BY isess.id DESC
    """),
    @NamedQuery(name = "instanceSession.getByInstanceIdAndProtocol", query = """
        SELECT isess
        FROM InstanceSession isess
        LEFT JOIN isess.instance i
        WHERE i.id = :instanceId
        AND isess.protocol = :protocol
        AND isess.current = true
        AND i.deletedAt IS NULL
        ORDER BY isess.id DESC
    """),
})
@Table(name = "instance_session")
public class InstanceSession extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "connection_id", length = 150, nullable = false)
    private String connectionId;

    @Column(name = "protocol", length = 150, nullable = true)
    private String protocol;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @Column(name = "current", nullable = false)
    private Boolean current;

    public InstanceSession() {
    }

    public InstanceSession(Instance instance, String protocol, String connectionId) {
        this.instance = instance;
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

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
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
}
