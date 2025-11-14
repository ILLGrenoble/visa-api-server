package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.*;

import java.util.List;

public record HypervisorInventory(Long hypervisorId, String hostname, long cpusAvailable, long memoryMBAvailable, List<HypervisorResource> resources) {

    public HypervisorInventory onInstanceReleased(final Instance instance) {
        final Flavour flavour = instance.getPlan().getFlavour();
        final List<InstanceDeviceAllocation> deviceAllocations = instance.getDeviceAllocations();

        long cpusAvailable = this.cpusAvailable + flavour.getCpu().longValue();
        long memoryMBAvailable = this.memoryMBAvailable + flavour.getMemory().longValue();
        List<HypervisorResource> resources = this.resources.stream()
            .map(resource -> {
                final InstanceDeviceAllocation instanceDeviceAllocation = deviceAllocations.stream()
                    .filter(deviceAllocation -> deviceAllocation.getDevicePool().getResourceClass() != null)
                    .filter(deviceAllocation -> deviceAllocation.getDevicePool().getResourceClass().equals(resource.getResourceClass()))
                    .findFirst()
                    .orElse(null);
                if (instanceDeviceAllocation != null) {
                    return resource.onDeviceReleased(instanceDeviceAllocation.getUnitCount());
                } else {
                    return resource;
                }
            })
            .toList();

        return new HypervisorInventory(hypervisorId, hostname, cpusAvailable, memoryMBAvailable, resources);
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
}
