package eu.ill.visa.core.entity;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@NamedQueries({
    @NamedQuery(name = "hypervisor.getAll", query = """
        SELECT h
        FROM Hypervisor h
        LEFT JOIN h.cloudProviderConfiguration cpc
        WHERE cpc.deletedAt IS NULL
        AND COALESCE(cpc.visible, true) = true
        ORDER BY h.id
    """),
})
@Table(name = "hypervisor")
public class Hypervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cloud_id", length = 250, nullable = false)
    private String cloudId;

    @Column(name = "hostname", length = 250, nullable = false)
    private String hostname;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
    private CloudProviderConfiguration cloudProviderConfiguration;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "hypervisor_id", foreignKey = @ForeignKey(name = "fk_hypervisor_resource_id"), nullable = false)
    private List<HypervisorResource> resources = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CloudProviderConfiguration getCloudProviderConfiguration() {
        return cloudProviderConfiguration;
    }

    public void setCloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
        this.cloudProviderConfiguration = cloudProviderConfiguration;
    }

    public List<HypervisorResource> getResources() {
        return resources;
    }

    public void setResources(List<HypervisorResource> resources) {
        this.resources = resources;
    }

    public static Builder Builder() {
        return new Builder();
    }

    public void updateResourceInventory(Map<String, Long> inventory) {
        // Remove resources that no longer exist
        List<HypervisorResource> removedResources = this.resources.stream().filter(resource -> !inventory.containsKey(resource.getResourceClass())).toList();
        this.resources.removeAll(removedResources);

        // Update or add resources
        List<HypervisorResource> newResources = new ArrayList<>();
        inventory.forEach((key, value) -> this.resources.stream()
            .filter(resource -> resource.getResourceClass().equals(key))
            .findFirst()
            .ifPresentOrElse(hypervisorResource -> {
                hypervisorResource.setTotal(value);
            }, () -> newResources.add(new HypervisorResource(key, value))));
        this.resources.addAll(newResources);
    }

    public void updateResourceUsage(Map<String, Long> usage) {
        // Update resource usage
        usage.forEach((key, value) -> this.resources.stream()
            .filter(resource -> resource.getResourceClass().equals(key))
            .findFirst()
            .ifPresent(hypervisorResource -> hypervisorResource.setUsage(value)));
    }

    public static final class Builder {
        private String cloudId;
        private String hostname;
        private String state;
        private String status;
        private Map<String, Long> resourceInventory;
        private CloudProviderConfiguration cloudProviderConfiguration;

        public Builder() {
        }

        public Builder cloudId(String cloudId) {
            this.cloudId = cloudId;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder resourceInventory(Map<String, Long> resourceInventory) {
            this.resourceInventory = resourceInventory;
            return this;
        }

        public Builder cloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
            this.cloudProviderConfiguration = cloudProviderConfiguration;
            return this;
        }

        public Hypervisor build() {
            Hypervisor hypervisor = new Hypervisor();
            hypervisor.setCloudId(cloudId);
            hypervisor.setHostname(hostname);
            hypervisor.setState(state);
            hypervisor.setStatus(status);
            hypervisor.setResources(this.resourceInventory.entrySet().stream()
                .map(entry -> new HypervisorResource(entry.getKey(), entry.getValue()))
                .toList());
            hypervisor.setCloudProviderConfiguration(cloudProviderConfiguration);
            return hypervisor;
        }
    }
}
