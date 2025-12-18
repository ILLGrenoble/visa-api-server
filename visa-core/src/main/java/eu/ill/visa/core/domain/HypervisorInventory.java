package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.*;

import java.util.List;

public record HypervisorInventory(Long hypervisorId, String hostname, long cpusAvailable, long totalCPUs,long memoryMBAvailable, long totalMemoryMB, List<HypervisorResource> resources, List<String> serverComputeIds) {

    public HypervisorInventory onResourcesModification(final ResourceUsageModifier resourceModifier) {
        long cpusAvailable = this.cpusAvailable - resourceModifier.cpuModifier();
        long memoryMBAvailable = this.memoryMBAvailable - resourceModifier.memoryModifier();
        List<HypervisorResource> resources = this.resources.stream()
            .map(resource -> {
                final ResourceUsageModifier.DeviceResourceUsageModifier deviceResourceModifier = resourceModifier.deviceResourceModifiers().stream()
                    .filter(modifier -> modifier.resourceClass() != null)
                    .filter(modifier -> modifier.resourceClass().equals(resource.getResourceClass()))
                    .findFirst()
                    .orElse(null);
                if (deviceResourceModifier != null) {
                    return resource.onDeviceUsageModification(deviceResourceModifier.modifier());
                } else {
                    return resource;
                }
            })
            .toList();

        List<String> serverComputeIds = this.serverComputeIds.stream()
            .filter(serverComputeId -> !resourceModifier.computeId().equals(serverComputeId))
            .toList();

        return new HypervisorInventory(hypervisorId, hostname, cpusAvailable, this.totalCPUs, memoryMBAvailable, this.totalMemoryMB, resources, serverComputeIds);
    }

    public boolean hasResourceClasses(List<String> requiredResourceClasses) {
        for (String requiredResourceClass : requiredResourceClasses) {
            final HypervisorResource hypervisorResource = this.resources.stream()
                .filter(resource -> resource.getResourceClass().equals(requiredResourceClass))
                .findFirst()
                .orElse(null);
            if (hypervisorResource == null) {
                return false;
            }
        }
        return true;
    }

    public long getAvailableResource(String resourceClass) {
        return this.resources.stream()
            .filter(resource -> resource.getResourceClass().equals(resourceClass))
            .map(HypervisorResource::getAvailable)
            .findFirst()
            .orElse(0L);
    }

    public long getTotalResource(String resourceClass) {
        return this.resources.stream()
            .filter(resource -> resource.getResourceClass().equals(resourceClass))
            .map(HypervisorResource::getTotal)
            .findFirst()
            .orElse(0L);
    }

    public boolean hostsServer(String serverComputeId) {
        return this.serverComputeIds.contains(serverComputeId);
    }
}
