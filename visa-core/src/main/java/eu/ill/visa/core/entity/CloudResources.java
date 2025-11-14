package eu.ill.visa.core.entity;


import jakarta.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name = "cloudResources.getAll", query = """
        SELECT cr
        FROM CloudResources cr
        LEFT JOIN cr.cloudProviderConfiguration cpc
        WHERE cpc.deletedAt IS NULL
        AND COALESCE(cpc.visible, true) = true
        ORDER BY cr.id
    """),
})
@Table(name = "cloud_resources")
public class CloudResources extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "vcpu_total", nullable = false)
    private Long vcpuTotal;

    @Column(name = "vcpu_usage", nullable = false)
    private Long vcpuUsage;

    @Column(name = "memory_mb_total", nullable = false)
    private Long memoryMbTotal;

    @Column(name = "memory_mb_usage", nullable = false)
    private Long memoryMbUsage;

    @Column(name = "instances_total", nullable = false)
    private Long instancesTotal;

    @Column(name = "instances_usage", nullable = false)
    private Long instancesUsage;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
    private CloudProviderConfiguration cloudProviderConfiguration;

    public static Builder Builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVcpuTotal() {
        return vcpuTotal;
    }

    public void setVcpuTotal(Long vcpuTotal) {
        this.vcpuTotal = vcpuTotal;
    }

    public Long getVcpuUsage() {
        return vcpuUsage;
    }

    public void setVcpuUsage(Long vcpuUsage) {
        this.vcpuUsage = vcpuUsage;
    }

    @Transient
    public Long getVcpuAvailable() {
        return this.vcpuTotal - this.vcpuUsage;
    }

    public Long getMemoryMbTotal() {
        return memoryMbTotal;
    }

    public void setMemoryMbTotal(Long memoryMbTotal) {
        this.memoryMbTotal = memoryMbTotal;
    }

    public Long getMemoryMbUsage() {
        return memoryMbUsage;
    }

    public void setMemoryMbUsage(Long memoryMbUsage) {
        this.memoryMbUsage = memoryMbUsage;
    }

    @Transient
    public Long getMemoryMBAvailable() {
        return this.memoryMbTotal - this.memoryMbUsage;
    }

    public Long getInstancesTotal() {
        return instancesTotal;
    }

    public void setInstancesTotal(Long instancesTotal) {
        this.instancesTotal = instancesTotal;
    }

    public Long getInstancesUsage() {
        return instancesUsage;
    }

    public void setInstancesUsage(Long instancesUsage) {
        this.instancesUsage = instancesUsage;
    }

    @Transient
    public Long getInstancesAvailable() {
        return this.instancesTotal - this.instancesUsage;
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

    public CloudResources onInstanceReleased(final Instance instance) {
        final Flavour flavour = instance.getPlan().getFlavour();
        return Builder()
            .vcpuTotal(vcpuTotal)
            .vcpuUsage(vcpuUsage - flavour.getCpu().longValue())
            .memoryMbTotal(memoryMbTotal)
            .memoryMbUsage(memoryMbUsage - flavour.getMemory())
            .instancesTotal(instancesTotal)
            .instancesUsage(instancesUsage - 1)
            .cloudProviderConfiguration(this.cloudProviderConfiguration)
            .build();
    }

    public static final class Builder {
        private Long vcpuTotal;
        private Long vcpuUsage;
        private Long memoryMbTotal;
        private Long memoryMbUsage;
        private Long instancesTotal;
        private Long instancesUsage;
        private CloudProviderConfiguration cloudProviderConfiguration;

        public Builder() {
        }

        public Builder vcpuTotal(Long vcpuTotal) {
            this.vcpuTotal = vcpuTotal;
            return this;
        }

        public Builder vcpuUsage(Long vcpuUsage) {
            this.vcpuUsage = vcpuUsage;
            return this;
        }

        public Builder memoryMbTotal(Long memoryMbTotal) {
            this.memoryMbTotal = memoryMbTotal;
            return this;
        }

        public Builder memoryMbUsage(Long memoryMbUsage) {
            this.memoryMbUsage = memoryMbUsage;
            return this;
        }

        public Builder instancesTotal(Long instancesTotal) {
            this.instancesTotal = instancesTotal;
            return this;
        }

        public Builder instancesUsage(Long instancesUsage) {
            this.instancesUsage = instancesUsage;
            return this;
        }

        public Builder cloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
            this.cloudProviderConfiguration = cloudProviderConfiguration;
            return this;
        }

        public CloudResources build() {
            CloudResources cloudResources = new CloudResources();
            cloudResources.vcpuTotal = vcpuTotal;
            cloudResources.vcpuUsage = vcpuUsage;
            cloudResources.memoryMbTotal = memoryMbTotal;
            cloudResources.memoryMbUsage = memoryMbUsage;
            cloudResources.instancesTotal = instancesTotal;
            cloudResources.instancesUsage = instancesUsage;
            cloudResources.cloudProviderConfiguration = cloudProviderConfiguration;
            return cloudResources;
        }
    }


}
