package eu.ill.visa.business.services;


import eu.ill.visa.cloud.domain.CloudResourceClass;
import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.domain.HypervisorInventory;
import eu.ill.visa.core.domain.SystemResources;
import eu.ill.visa.core.entity.CloudResources;
import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.Hypervisor;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Singleton
public class FlavourAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(FlavourAvailabilityService.class);

    private final FlavourService flavourService;
    private final HypervisorService hypervisorService;
    private final CloudResourcesService cloudResourcesService;
    private final DevicePoolService devicePoolService;

    @Inject
    public FlavourAvailabilityService(final FlavourService flavourService,
                                      final HypervisorService hypervisorService,
                                      final CloudResourcesService cloudResourcesService,
                                      final DevicePoolService devicePoolService) {
        this.flavourService = flavourService;
        this.hypervisorService = hypervisorService;
        this.cloudResourcesService = cloudResourcesService;
        this.devicePoolService = devicePoolService;
    }

    public List<FlavourAvailability> getAllAvailabilities() {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        final Map<Long, SystemResources> allSystemResource = this.getAllSystemResources();

        return flavours.stream()
            .map(flavour -> this.getAvailability(flavour, allSystemResource))
            .toList();
    }

    public FlavourAvailability getAvailability(final Flavour flavour) {
        final Map<Long, SystemResources> allSystemResource = this.getAllSystemResources();

        return this.getAvailability(flavour, allSystemResource);
    }

    public FlavourAvailability getAvailability(final Flavour flavour, final  Map<Long, SystemResources> allSystemResource) {
        final Long cloudId = flavour.getCloudId();
        final SystemResources systemResources = allSystemResource.get(cloudId);
        if (systemResources == null) {
            // We don't know anything so return unknown response
            return new FlavourAvailability(flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);

        } else {
            return systemResources.getAvailability(flavour);
        }
    }

    private Map<Long, SystemResources> getAllSystemResources() {
        final Map<Long, CloudResources> allCloudResources = this.getCloudResources();
        final Map<Long, List<Hypervisor>> cloudHypervisors = this.getCloudHypervisors();
        List<DevicePoolUsage> devicePoolUsages = devicePoolService.getDevicePoolUsage();

        Set<Long> allCloudClientIds = new HashSet<>(allCloudResources.keySet());
        allCloudClientIds.addAll(cloudHypervisors.keySet());

        return allCloudClientIds.stream()
            .collect(Collectors.toMap(
                cloudClientId -> cloudClientId,
                cloudClientId -> {
                    final CloudResources cloudResources = allCloudResources.get(cloudClientId);
                    final List<Hypervisor> hypervisors = cloudHypervisors.get(cloudClientId);
                    final List<HypervisorInventory> hypervisorInventories = hypervisors == null ? new ArrayList<>() : hypervisors.stream()
                        .map(hypervisor -> {
                            long cpusAvailable = hypervisor.getAvailableResource(CloudResourceClass.VCPU_RESOURCE_CLASS);
                            long ramMBAvailable = hypervisor.getAvailableResource(CloudResourceClass.MEMORY_MB_RESOURCE_CLASS);
                            return new HypervisorInventory(hypervisor.getId(), hypervisor.getHostname(), cpusAvailable, ramMBAvailable, hypervisor.getResources());
                        })
                        .toList();

                    return new SystemResources(cloudClientId, cloudResources, devicePoolUsages, hypervisorInventories);
                }
            ));
    }


    private Map<Long, CloudResources> getCloudResources() {
        return this.cloudResourcesService.getAll().stream()
            .collect(Collectors.toMap(
                cloudResources -> cloudResources.getCloudId() == null ? - 1 : cloudResources.getCloudId(),
                cloudResources -> cloudResources
            ));
    }

    private Map<Long, List<Hypervisor>> getCloudHypervisors() {
        final List<Hypervisor> hypervisors = this.hypervisorService.getAllAvailable();
        return hypervisors.stream()
            .collect(Collectors.groupingBy(hypervisor -> hypervisor.getCloudId() == null ? -1 : hypervisor.getCloudId()));
    }

}
