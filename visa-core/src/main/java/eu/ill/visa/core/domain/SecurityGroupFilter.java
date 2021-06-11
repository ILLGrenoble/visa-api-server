package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.util.Objects.requireNonNull;

public class SecurityGroupFilter {

    private Long id;

    private Long objectId;

    private String objectType;

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
