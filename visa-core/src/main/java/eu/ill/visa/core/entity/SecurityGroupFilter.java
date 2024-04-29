package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.util.Objects.requireNonNull;

@Entity
@NamedQueries({
    @NamedQuery(name = "securityGroupFilter.getById", query = """
            SELECT l FROM SecurityGroupFilter l WHERE l.id = :id
    """),
    @NamedQuery(name = "securityGroupFilter.securityGroupFilterBySecurityIdAndObjectIdAndType", query = """
               SELECT l FROM SecurityGroupFilter l
               JOIN l.securityGroup sg
               WHERE sg.id = :securityGroupId AND l.objectId = :objectId AND l.objectType = :objectType
    """),
    @NamedQuery(name = "securityGroupFilter.getAll", query = """
               SELECT l FROM SecurityGroupFilter l
    """),
    @NamedQuery(name = "securityGroupFilter.countAll", query = """
               SELECT count(sgf.id) FROM SecurityGroupFilter sgf
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
