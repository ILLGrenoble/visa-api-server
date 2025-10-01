package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.DeviceType;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "devicePool.getById", query = """
            SELECT dp
            FROM DevicePool dp
            LEFT JOIN dp.cloudProviderConfiguration cpc
            WHERE dp.id = :id
            AND dp.deletedAt IS NULL
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "devicePool.getComputeIdentifierAndType", query = """
            SELECT dp
            FROM DevicePool dp
            LEFT JOIN dp.cloudProviderConfiguration cpc
            WHERE dp.computeIdentifier = :computeIdentifier
            AND dp.deviceType = :deviceType
            AND dp.deletedAt IS NULL
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "devicePool.getAll", query = """
            SELECT dp
            FROM DevicePool dp
            LEFT JOIN dp.cloudProviderConfiguration cpc
            WHERE dp.deletedAt IS NULL
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "devicePool.getDevicePoolUsage", query = """
            SELECT new eu.ill.visa.core.entity.partial.DevicePoolUsage(dp.id, dp.name, dp.totalUnits, COALESCE(SUM(CASE WHEN i IS NOT NULL THEN a.unitCount ELSE 0 END), 0))
            FROM DevicePool dp
            LEFT OUTER JOIN InstanceDeviceAllocation a on a.devicePool = dp
            LEFT OUTER JOIN Instance i on a.instance = i AND i.deletedAt IS NULL
            WHERE dp.deletedAt IS NULL
            GROUP BY dp.id, dp.name
            ORDER BY dp.name
    """),
})
@Table(name = "device_pool")
public class DevicePool extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "compute_identifier", length = 250, nullable = false)
    private String computeIdentifier;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 255, nullable = true)
    private DeviceType deviceType;

    @Column(name = "deleted_at", nullable = true)
    private Date deletedAt;

    // The number of unit devices available in the pool. Null implies managed automatically or un-managed (ignored)
    @Column(name = "total_units", nullable = true)
    private Integer totalUnits;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
    private CloudProviderConfiguration cloudProviderConfiguration;

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

    public String getComputeIdentifier() {
        return computeIdentifier;
    }

    public void setComputeIdentifier(String computeIdentifier) {
        this.computeIdentifier = computeIdentifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
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

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DevicePool devicePool = (DevicePool) o;

        return new EqualsBuilder()
            .append(id, devicePool.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }


    public static final class Builder {
        private Long id;
        private String name;
        private String description;
        private DeviceType deviceType;
        private String computeIdentifier;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder deviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder computeIdentifier(String computeIdentifier) {
            this.computeIdentifier = computeIdentifier;
            return this;
        }

        public DevicePool build() {
            DevicePool devicePool = new DevicePool();
            devicePool.setId(id);
            devicePool.setName(name);
            devicePool.setDescription(description);
            devicePool.setDeviceType(deviceType);
            devicePool.setComputeIdentifier(computeIdentifier);
            return devicePool;
        }
    }
}
