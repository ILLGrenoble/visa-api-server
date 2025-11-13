package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.FlavourDevice;
import eu.ill.visa.core.entity.HypervisorResource;

import java.util.List;

public record HypervisorInventory(Long hypervisorId, String hostname, long cpusAvailable, long memoryMBAvailable, List<HypervisorResource> resources) {

    public HypervisorInventory onFlavourReleased(final Flavour flavour) {
        long cpusAvailable = this.cpusAvailable + flavour.getCpu().longValue();
        long memoryMBAvailable = this.memoryMBAvailable + flavour.getMemory().longValue();
        List<FlavourDevice> flavourDevices = flavour.getDevices();
        List<HypervisorResource> resources = this.resources.stream()
            .map(resource -> {
                final FlavourDevice flavourDevice = flavourDevices.stream()
                    .filter(fd -> fd.getDevicePool().getResourceClass() != null)
                    .filter(fd -> fd.getDevicePool().getResourceClass().equals(resource.getResourceClass()))
                    .findFirst()
                    .orElse(null);
                if (flavourDevice != null) {
                    return resource.onDeviceReleased(flavourDevice.getUnitCount());
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
