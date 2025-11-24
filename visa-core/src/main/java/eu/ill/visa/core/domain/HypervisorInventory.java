package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.*;

import java.util.List;

public record HypervisorInventory(Long hypervisorId, String hostname, long cpusAvailable, long totalCPUs,long memoryMBAvailable, long totalMemoryMB, List<HypervisorResource> resources, List<String> serverComputeIds) {

    public HypervisorInventory onInstanceReleased(final Instance instance) {
        final Flavour flavour = instance.getPlan().getFlavour();
//        final List<InstanceDeviceAllocation> deviceAllocations = instance.getDeviceAllocations();
        final List<FlavourDevice> flavourDevices = flavour.getDevices();

        long cpusAvailable = this.cpusAvailable + flavour.getCpu().longValue();
        long memoryMBAvailable = this.memoryMBAvailable + flavour.getMemory().longValue();
        List<HypervisorResource> resources = this.resources.stream()
            .map(resource -> {
//                final InstanceDeviceAllocation instanceDeviceAllocation = deviceAllocations.stream()
//                    .filter(deviceAllocation -> deviceAllocation.getDevicePool().getResourceClass() != null)
//                    .filter(deviceAllocation -> deviceAllocation.getDevicePool().getResourceClass().equals(resource.getResourceClass()))
//                    .findFirst()
//                    .orElse(null);
//                if (instanceDeviceAllocation != null) {
//                    return resource.onDeviceReleased(instanceDeviceAllocation.getUnitCount());
//                } else {
//                    return resource;
//                }
                final FlavourDevice flavourDevice = flavourDevices.stream()
                    .filter(aFlavourDevice -> aFlavourDevice.getDevicePool().getResourceClass() != null)
                    .filter(aFlavourDevice -> aFlavourDevice.getDevicePool().getResourceClass().equals(resource.getResourceClass()))
                    .findFirst()
                    .orElse(null);
                if (flavourDevice != null) {
                    return resource.onDeviceReleased(flavourDevice.getUnitCount());
                } else {
                    return resource;
                }
            })
            .toList();

        List<String> serverComputeIds = this.serverComputeIds.stream()
            .filter(serverComputeId -> !instance.getComputeId().equals(serverComputeId))
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
