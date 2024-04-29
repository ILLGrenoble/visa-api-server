package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "security_group")
public class SecurityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
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
