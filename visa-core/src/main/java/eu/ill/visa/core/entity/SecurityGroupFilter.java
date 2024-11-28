package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.util.Objects.requireNonNull;

@Entity
@NamedQueries({
    @NamedQuery(name = "securityGroupFilter.getById", query = """
        SELECT sgf
        FROM SecurityGroupFilter sgf
        JOIN SecurityGroup sg ON sgf.securityGroup = sg
        LEFT JOIN sg.cloudProviderConfiguration cpc
        WHERE sgf.id = :id
        AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "securityGroupFilter.securityGroupFilterBySecurityIdAndObjectIdAndType", query = """
        SELECT sgf
        FROM SecurityGroupFilter sgf
        JOIN sgf.securityGroup sg ON sgf.securityGroup = sg
        LEFT JOIN sg.cloudProviderConfiguration cpc
        WHERE sg.id = :securityGroupId
        AND sgf.objectId = :objectId
        AND sgf.objectType = :objectType
        AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "securityGroupFilter.getAll", query = """
        SELECT sgf
        FROM SecurityGroupFilter sgf
        JOIN SecurityGroup sg ON sgf.securityGroup = sg
        LEFT JOIN sg.cloudProviderConfiguration cpc
        WHERE cpc.deletedAt IS NULL
        ORDER BY sgf.objectType
    """),
    @NamedQuery(name = "securityGroupFilter.countAll", query = """
        SELECT count(sgf.id)
        FROM SecurityGroupFilter sgf
        JOIN SecurityGroup sg ON sgf.securityGroup = sg
        LEFT JOIN sg.cloudProviderConfiguration cpc
        WHERE cpc.deletedAt IS NULL
    """),
})
@Table(name = "security_group_filter")
public class SecurityGroupFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "object_type", nullable = false)
    private String objectType;

    @ManyToOne
    @JoinColumn(name = "security_group_id", foreignKey = @ForeignKey(name = "fk_security_group_id"), nullable = false)
    private SecurityGroup securityGroup;

    public SecurityGroupFilter() {

    }

    public SecurityGroupFilter(final SecurityGroup securityGroup, final Long objectId, final String objectType) {
        this.securityGroup = requireNonNull(securityGroup, "securityGroup cannot be null");
        this.objectId = requireNonNull(objectId, "objectId cannot be null");
        this.objectType = requireNonNull(objectType, "objectType cannot be null");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SecurityGroupFilter that = (SecurityGroupFilter) o;

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
            .append("objectId", objectId)
            .append("objectType", objectType)
            .append("securityGroup", securityGroup)
            .toString();
    }


    public static final class Builder {
        private Long    objectId;
        private String  objectType;
        private SecurityGroup securityGroup;

        public Builder() {
        }

        public Builder objectId(Long objectId) {
            this.objectId = objectId;
            return this;
        }

        public Builder objectType(String objectType) {
            this.objectType = objectType;
            return this;
        }

        public Builder securityGroup(SecurityGroup securityGroup) {
            this.securityGroup = securityGroup;
            return this;
        }

        public SecurityGroupFilter build() {
            SecurityGroupFilter securityGroupFilter = new SecurityGroupFilter();
            securityGroupFilter.setObjectId(objectId);
            securityGroupFilter.setObjectType(objectType);
            securityGroupFilter.setSecurityGroup(securityGroup);
            return securityGroupFilter;
        }
    }
}
