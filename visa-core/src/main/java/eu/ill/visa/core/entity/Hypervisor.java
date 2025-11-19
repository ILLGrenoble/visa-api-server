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
    @NamedQuery(name = "hypervisor.countAll", query = """
        SELECT COUNT(h)
        FROM Hypervisor h
        LEFT JOIN h.cloudProviderConfiguration cpc
        WHERE cpc.deletedAt IS NULL
        AND COALESCE(cpc.visible, true) = true
    """),
    @NamedQuery(name = "hypervisor.getAllAvailable", query = """
        SELECT h
        FROM Hypervisor h
        LEFT JOIN h.cloudProviderConfiguration cpc
        WHERE cpc.deletedAt IS NULL
        AND COALESCE(cpc.visible, true) = true
        AND h.state = 'up'
        AND h.status = 'enabled'
        ORDER BY h.id
    """),
})
@Table(name = "hypervisor")
public class Hypervisor extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "compute_id", length = 250, nullable = false)
    private String computeId;

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

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "hypervisor_id", foreignKey = @ForeignKey(name = "fk_hypervisor_allocation_id"), nullable = false)
    private List<HypervisorAllocation> allocations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
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

    public List<HypervisorAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<HypervisorAllocation> allocations) {
        this.allocations = allocations;
    }

    @Transient
    public Long getCloudId() {
        return this.cloudProviderConfiguration == null ? null : this.cloudProviderConfiguration.getId();
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

    public void updateAllocations(List<HypervisorAllocation> currentAllocations) {
        // Remove allocations that no longer exist
        List<HypervisorAllocation> removedAllocations = this.allocations.stream()
            .filter(allocation -> currentAllocations.stream()
                .filter(existingAllocation -> existingAllocation.getServerComputeId().equals(allocation.getServerComputeId()))
                .findFirst()
                .isEmpty()
            )
            .toList();
        this.allocations.removeAll(removedAllocations);

        // Update (nothing done yet) or add allocations
        List<HypervisorAllocation> newAllocations = new ArrayList<>();
        currentAllocations.forEach(allocation -> this.allocations.stream()
            .filter(existingAllocation -> existingAllocation.getServerComputeId().equals(allocation.getServerComputeId()))
            .findFirst()
            .ifPresentOrElse(existingAllocation -> { /* no action yet */ }, () -> newAllocations.add(new HypervisorAllocation(allocation.getServerComputeId()))));
        this.allocations.addAll(newAllocations);
    }

    public long getAvailableResource(String resourceClass) {
        return this.resources.stream()
            .filter(resource -> resource.getResourceClass().equals(resourceClass))
            .map(HypervisorResource::getAvailable)
            .findFirst()
            .orElse(0L);
    }

    public static final class Builder {
        private String computeId;
        private String hostname;
        private String state;
        private String status;
        private Map<String, Long> resourceInventory;
        private CloudProviderConfiguration cloudProviderConfiguration;

        public Builder() {
        }

        public Builder computeId(String computeId) {
            this.computeId = computeId;
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
            hypervisor.setComputeId(computeId);
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
