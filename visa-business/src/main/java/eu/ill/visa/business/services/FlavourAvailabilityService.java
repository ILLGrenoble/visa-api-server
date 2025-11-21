package eu.ill.visa.business.services;


import eu.ill.visa.cloud.domain.CloudResourceClass;
import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.domain.HypervisorInventory;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.SystemResources;
import eu.ill.visa.core.domain.filters.InstanceFilter;
import eu.ill.visa.core.entity.*;
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
    private final InstanceService instanceService;

    @Inject
    public FlavourAvailabilityService(final FlavourService flavourService,
                                      final HypervisorService hypervisorService,
                                      final CloudResourcesService cloudResourcesService,
                                      final DevicePoolService devicePoolService,
                                      final InstanceService instanceService) {
        this.flavourService = flavourService;
        this.hypervisorService = hypervisorService;
        this.cloudResourcesService = cloudResourcesService;
        this.devicePoolService = devicePoolService;
        this.instanceService = instanceService;
    }

    public List<FlavourAvailability> getAllCurrentAvailabilities() {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        final Map<Long, SystemResources> allSystemResource = this.getAllSystemResources();

        return flavours.stream()
            .map(flavour -> {
                Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                return this.getAvailability(flavour, allSystemResource.get(cloudId));
            })
            .toList();
    }

    public FlavourAvailability getCurrentAvailability(final Flavour flavour) {
        final Map<Long, SystemResources> allSystemResource = this.getAllSystemResources();

        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

        return this.getAvailability(flavour, allSystemResource.get(cloudId));
    }

    public List<FlavourAvailability> getAllFirstAvailabilities() {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        final Map<Long, SystemResources> allSystemResource = this.getAllSystemResources();

        return flavours.stream()
            .map(flavour -> {
                Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                return this.getFirstAvailability(flavour, allSystemResource.get(cloudId));
            })
            .toList();
    }

    public FlavourAvailability getFirstAvailability(final Flavour flavour) {
        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

        SystemResources systemResources = this.getAllSystemResources().get(cloudId);
        return this.getFirstAvailability(flavour, systemResources);
    }

    public List<FlavourAvailability> getFutureAvailabilities(final Flavour flavour) {
        final Map<Long, SystemResources> allSystemResource = this.getAllSystemResources();

        final Map<Long, List<Instance>> cloudInstances = this.getCloudInstances();

        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

        return this.getFutureAvailabilities(flavour, allSystemResource.get(cloudId), cloudInstances.get(cloudId));
    }

    public Map<Flavour, List<FlavourAvailability>> getAllFutureAvailabilities() {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        final Map<Long, SystemResources> allSystemResource = this.getAllSystemResources();
        final Map<Long, List<Instance>> cloudInstances = this.getCloudInstances();

        return flavours.stream()
            .collect(Collectors.toMap(
                flavour -> flavour,
                flavour -> {
                    Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                    return this.getFutureAvailabilities(flavour, allSystemResource.get(cloudId), cloudInstances.get(cloudId));
                }
            ));
    }


    private FlavourAvailability getAvailability(final Flavour flavour, final SystemResources systemResources) {
        if (systemResources == null) {
            // We don't know anything so return unknown response
            return new FlavourAvailability(new Date(), flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);

        } else {
            return systemResources.getAvailability(flavour);
        }
    }

    private FlavourAvailability getFirstAvailability(final Flavour flavour, final SystemResources systemResources) {
        // Check for immediate availability
        FlavourAvailability availability = this.getAvailability(flavour, systemResources);
        if (!availability.isAvailable().equals(FlavourAvailability.AvailabilityState.NO)) {
            return availability;
        }

        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();
        SystemResources futureSystemResources = systemResources;

        final List<Instance> cloudInstances = this.getCloudInstances().get(cloudId);
        while (cloudInstances != null && !cloudInstances.isEmpty() && availability.hasUnits().equals(FlavourAvailability.AvailabilityState.NO)) {
            final Instance instance = cloudInstances.removeFirst();
            futureSystemResources = futureSystemResources.onInstanceDeleted(instance);
            availability = this.getAvailability(flavour, futureSystemResources);
        }

        return availability;

    }

    private List<FlavourAvailability> getFutureAvailabilities(final Flavour flavour, SystemResources systemResources, final List<Instance> instances) {
        if (systemResources == null) {
            // We don't know anything so return unknown response
            return List.of(new FlavourAvailability(new Date(), flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN));

        } else {
            List<FlavourAvailability> futures = new ArrayList<>();

            // Initialise array with the current availability
            futures.add(systemResources.getAvailability(flavour));

            if (instances != null) {
                for (Instance instance : instances) {
                    systemResources = systemResources.onInstanceDeleted(instance);
                    futures.add(systemResources.getAvailability(flavour));
                }
            }
            return futures;
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
                            List<String> serverComputeIds = hypervisor.getAllocations().stream().map(HypervisorAllocation::getServerComputeId).toList();
                            return new HypervisorInventory(hypervisor.getId(), hypervisor.getHostname(), cpusAvailable, ramMBAvailable, hypervisor.getResources(), serverComputeIds);
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

    private Map<Long, List<Instance>> getCloudInstances() {
        final List<Instance> instances = this.instanceService.getAll(new InstanceFilter(), new OrderBy("terminationDate", true)).stream()
            .filter(instance -> instance.getTerminationDate() != null)
            .toList();
        return instances.stream()
            .collect(Collectors
                .groupingBy(instance -> {
                    final Flavour flavour = instance.getPlan().getFlavour();
                    return flavour.getCloudId() == null ? -1 : flavour.getCloudId();
                }));
    }

}
