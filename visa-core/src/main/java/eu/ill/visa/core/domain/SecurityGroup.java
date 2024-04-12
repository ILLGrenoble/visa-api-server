package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Transient;

public class SecurityGroup {

    private Long id;

    private String name;

    private CloudProviderConfiguration cloudProviderConfiguration;

    public SecurityGroup() {
    }

    public SecurityGroup(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CloudProviderConfiguration getCloudProviderConfiguration() {
        return cloudProviderConfiguration;
    }

    public void setCloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
        this.cloudProviderConfiguration = cloudProviderConfiguration;
    }

    @Transient
    public Long getCloudId() {
        return this.cloudProviderConfiguration == null ? null : this.cloudProviderConfiguration.getId();
    }

    public boolean hasSameCloudClientId(Long cloudClientId) {
        if (this.cloudProviderConfiguration == null) {
            return cloudClientId == null || cloudClientId.equals(-1L);

        } else {
            return this.cloudProviderConfiguration.getId().equals(cloudClientId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SecurityGroup that = (SecurityGroup) o;

        return new EqualsBuilder()
            .append(name, that.name)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .toHashCode();
    }
}
