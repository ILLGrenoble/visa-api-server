package eu.ill.visa.core.domain;


import eu.ill.visa.core.domain.FlavourAvailability.AvailabilityData;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record SystemResources(Long cloudId, Date availabilityDate, CloudResources cloudResources, List<DevicePoolUsage> devicePoolUsages, List<HypervisorInventory> hypervisorInventories, FlavourUsages flavourUsages, Set<Long> bookedResourcesIds) {
    private static final Logger logger = LoggerFactory.getLogger(SystemResources.class);

    private record FlavourResourceRequirement(Long cloudId, Flavour flavour,  Long vcpus, Long memoryMB, List<FlavourDevice> flavourDevices) {
        boolean hasDevices() {
            return !this.flavourDevices.isEmpty();
        }
    }

    public SystemResources(Long cloudId, Date availabilityDate, CloudResources cloudResources, List<DevicePoolUsage> devicePoolUsages, List<HypervisorInventory> hypervisorInventories, FlavourUsages flavourUsages, Set<Long> bookedResourcesIds) {
        this.cloudId = cloudId;
        this.availabilityDate = availabilityDate;
        this.cloudResources = cloudResources;
        this.devicePoolUsages = devicePoolUsages;
        this.hypervisorInventories = hypervisorInventories;
        this.flavourUsages = flavourUsages;
        this.bookedResourcesIds = new HashSet<>(bookedResourcesIds); // Ensure we clone the booked Resource Ids as we modify them later
    }

    public SystemResources(Long cloudId, CloudResources cloudResources, List<DevicePoolUsage> devicePoolUsages, List<HypervisorInventory> hypervisorInventories, FlavourUsages flavourUsages) {
        this(cloudId, new Date(), cloudResources, devicePoolUsages, hypervisorInventories, flavourUsages, new HashSet<>());
    }


    public SystemResources onResourcesModification(final ResourceUsageModifier resourceModifier) {
        Date date = resourceModifier.modificationDate();

        CloudResources cloudResources = this.cloudResources == null ? null : this.cloudResources.onResourcesModification(resourceModifier);

        FlavourUsages modifiedFlavourUsages = this.flavourUsages.combine(resourceModifier.flavourUsages());

        List<DevicePoolUsage> devicePoolUsages = this.devicePoolUsages.stream()
            .map(devicePoolUsage -> {
                final ResourceUsageModifier.DeviceResourceUsageModifier deviceResourceModifier = resourceModifier.deviceResourceModifiers().stream()
                    .filter(modifier -> modifier.devicePoolId() == devicePoolUsage.getDevicePoolId())
                    .findFirst()
                    .orElse(null);

                if (deviceResourceModifier == null) {
                    return devicePoolUsage;
                } else {
                    return devicePoolUsage.onUnitsModified(deviceResourceModifier.modifier());
                }
            })
            .toList();

        if (resourceModifier.computeId() != null) {
            List<HypervisorInventory> hypervisorInventories = this.hypervisorInventories.stream()
                .map(inventory -> {
                    if (inventory.hostsServer(resourceModifier.computeId())) {
                        return inventory.onResourcesModification(resourceModifier);
                    } else {
                        return inventory;
                    }

                }).toList();

            return new SystemResources(cloudId, date, cloudResources, devicePoolUsages, hypervisorInventories, modifiedFlavourUsages, bookedResourcesIds);

        } else if (resourceModifier.associatedBookingId() != null) {
            // Compute Id unknown (so hypervisor can't be determined) but bookingId known so we can track when the hypervisor data will again be valid (ie, at the end of the booking)
            // Maintain hypervisor data, update it whenever possible, put not that the system resources are in an UNCERTAIN state

            // If resource usage increases then add the associatedBookingId (otherwise remove it)
            Set<Long> modifiedBookedResourcesIds = new HashSet<>(this.bookedResourcesIds);
            if (resourceModifier.instanceModifier() > 0) {
                modifiedBookedResourcesIds.add(resourceModifier.associatedBookingId());

            } else {
                modifiedBookedResourcesIds.remove(resourceModifier.associatedBookingId());
            }

            return new SystemResources(cloudId, date, cloudResources, devicePoolUsages, hypervisorInventories, modifiedFlavourUsages, modifiedBookedResourcesIds);

        } else {
            // Compute Id unknown, so hypervisor can't be determined: from here-on we cannot precisely determine the
            // hypervisor inventories after the modification. Don't know anything about booked Resource Ids either.
            // Therefor empty all hypervisor inventories since they can't be used to evaluate the system resources

            return new SystemResources(cloudId, date, cloudResources, devicePoolUsages, new ArrayList<>(), modifiedFlavourUsages, bookedResourcesIds);
        }

    }

    public FlavourAvailability getAvailability(final Flavour flavour) {
        FlavourResourceRequirement resourceRequirements = this.requirementsForFlavour(flavour);

        if (resourceRequirements.hasDevices()) {
            return this.getAvailabilityForDeviceFlavour(resourceRequirements);

        } else {
            return this.getAvailabilityForSimpleFlavour(resourceRequirements);
        }
    }

    private FlavourResourceRequirement requirementsForFlavour(final Flavour flavour) {
        long vcpus = (long)Math.ceil(flavour.getCpu());
        long memoryMB = (long)flavour.getMemory();

        return new FlavourResourceRequirement(flavour.getCloudId() == null ? -1 : flavour.getCloudId(), flavour, vcpus, memoryMB, flavour.getDevices());
    }


    private FlavourAvailability getAvailabilityForSimpleFlavour(final FlavourResourceRequirement flavourResourceRequirement) {

        final Long flavourUsage = this.flavourUsages.getFlavourUsage(flavourResourceRequirement.flavour);

        FlavourAvailability flavourAvailability = null;
        if (hypervisorInventories != null && !hypervisorInventories.isEmpty() && bookedResourcesIds.isEmpty()) {
            // Determine total number of units possible
            AvailabilityData availabilityData = hypervisorInventories.stream()
                .map(hypervisorInventory -> {
                    long vcpusAvailable = hypervisorInventory.cpusAvailable();
                    long ramMBAvailable = hypervisorInventory.memoryMBAvailable();
                    long totalCPUs = hypervisorInventory.totalCPUs();
                    long totalMemoryMB = hypervisorInventory.totalMemoryMB();

                    long vcpuUnitsAvailable = vcpusAvailable / flavourResourceRequirement.vcpus;
                    long ramUnitsAvailable = ramMBAvailable / flavourResourceRequirement.memoryMB;
                    long vcpuUnitsTotal = totalCPUs / flavourResourceRequirement.vcpus;
                    long ramUnitsTotal = totalMemoryMB / flavourResourceRequirement.memoryMB;


                    long hypervisorAvailableUnits = Math.min(vcpuUnitsAvailable, ramUnitsAvailable);
                    long hypervisorTotalUnits = Math.min(vcpuUnitsTotal, ramUnitsTotal);

                    logger.debug("Flavour {} (RAM_MB {} vCPUs {}), hypervisor {} RAM_MB {} vCPUs {}, available Units {}", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus, hypervisorInventory.hostname(), ramMBAvailable, vcpusAvailable, hypervisorAvailableUnits);

                    return new AvailabilityData(hypervisorAvailableUnits, hypervisorTotalUnits, flavourUsage);

                })
                .reduce(new AvailabilityData(0L, 0L, flavourUsage), (acc, next) -> {
                  return new  AvailabilityData(acc.available() + next.available(), acc.total() + next.total(), flavourUsage);
                });

            flavourAvailability = new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.of(availabilityData), FlavourAvailability.AvailabilityConfidence.CERTAIN);
        }

        if (cloudResources != null) {
            AvailabilityData cloudAvailability = this.getAvailableUnitsFromCloudResources(flavourResourceRequirement);
            logger.debug("Flavour {} (RAM_MB {} vCPUs {}), available Units {}", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus, cloudAvailability.available());

            if (flavourAvailability != null && flavourAvailability.availability().isPresent()) {
                long unitsAvailable = Math.min(cloudAvailability.available(), flavourAvailability.availability().get().available());
                long unitsTotal = Math.min(cloudAvailability.total(), flavourAvailability.availability().get().total());
                AvailabilityData combinedAvailability = new AvailabilityData(unitsAvailable, unitsTotal, flavourUsage);

                flavourAvailability = new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.of(combinedAvailability), FlavourAvailability.AvailabilityConfidence.CERTAIN);

            } else {
                flavourAvailability = new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.of(cloudAvailability), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
            }
        }

        if (flavourAvailability == null) {
            // If we have no hypervisor information nor cloud limit information then we know nothing about the availability
            flavourAvailability = new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
        }

        return flavourAvailability;
    }


    private FlavourAvailability getAvailabilityForDeviceFlavour(final FlavourResourceRequirement flavourResourceRequirement) {

        final Long flavourUsage = this.flavourUsages.getFlavourUsage(flavourResourceRequirement.flavour);

        FlavourAvailability flavourAvailability = null;
        if (hypervisorInventories != null && !hypervisorInventories.isEmpty() && bookedResourcesIds.isEmpty()) {
            flavourAvailability = this.getDeviceFlavourAvailabilityWithHypervisor(flavourResourceRequirement);
        }

        if (cloudResources != null) {
            AvailabilityData cloudAvailability = this.getAvailableUnitsFromCloudResources(flavourResourceRequirement);

            if (flavourAvailability != null && flavourAvailability.availability().isPresent()) {
                // Make sure cloud limits are taken into account even when hypervisor data in known
                long unitsAvailable = Math.min(cloudAvailability.available(), flavourAvailability.availability().get().available());
                long unitsTotal = Math.min(cloudAvailability.total(), flavourAvailability.availability().get().total());
                AvailabilityData combinedAvailability = new AvailabilityData(unitsAvailable, unitsTotal, flavourUsage);
                flavourAvailability = new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.of(combinedAvailability), FlavourAvailability.AvailabilityConfidence.CERTAIN);

            } else {
                // Check device availability from
                Optional<AvailabilityData> deviceAvailability = this.getDeviceAvailability(flavourResourceRequirement.flavourDevices);
                flavourAvailability = this.convertDeviceAvailabilityToFlavourAvailability(flavourResourceRequirement, cloudAvailability, deviceAvailability.orElse(null));
            }
        }

        if (flavourAvailability == null) {
            // If we have no hypervisor information nor cloud limit information then we know nothing about the availability
            flavourAvailability = new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
        }

        return flavourAvailability;
    }


    private AvailabilityData getAvailableUnitsFromCloudResources(final FlavourResourceRequirement flavourResourceRequirement) {
        final Long flavourUsage = this.flavourUsages.getFlavourUsage(flavourResourceRequirement.flavour);

        long cloudVCPUsAvailable = cloudResources.getVcpuAvailable();
        long cloudMemoryMBAvailable = cloudResources.getMemoryMBAvailable();
        long cloudVCPUsTotal = cloudResources.getVcpuTotal();
        long cloudMemoryMBTotal = cloudResources.getMemoryMbTotal();

        long vcpuUnitsAvailable = cloudVCPUsAvailable / flavourResourceRequirement.vcpus;
        long ramUnitsAvailable = cloudMemoryMBAvailable / flavourResourceRequirement.memoryMB;
        long instanceUnitsAvailable = cloudResources.getInstancesAvailable();

        long vcpuUnitsTotal = cloudVCPUsTotal / flavourResourceRequirement.vcpus;
        long ramUnitsTotal = cloudMemoryMBTotal / flavourResourceRequirement.memoryMB;
        long instanceUnitsTotal = cloudResources.getInstancesTotal();

        long unitsAvailable = Math.min(instanceUnitsAvailable, Math.min(vcpuUnitsAvailable, ramUnitsAvailable));
        long unitsTotal = Math.min(instanceUnitsTotal, Math.min(vcpuUnitsTotal, ramUnitsTotal));
        return new AvailabilityData(unitsAvailable, unitsTotal, flavourUsage);
    }


    private FlavourAvailability getDeviceFlavourAvailabilityWithHypervisor(final FlavourResourceRequirement flavourResourceRequirement) {
        final Long flavourUsage = this.flavourUsages.getFlavourUsage(flavourResourceRequirement.flavour);

        List<FlavourDevice> managedDevices = flavourResourceRequirement.flavourDevices.stream()
            .filter(flavourDevice -> flavourDevice.getDevicePool().getResourceClass() != null)
            .toList();

        List<FlavourDevice> unmanagedDevices = flavourResourceRequirement.flavourDevices.stream()
            .filter(flavourDevice -> flavourDevice.getDevicePool().getResourceClass() == null)
            .toList();


        AvailabilityData availabilityData = hypervisorInventories.stream()
            .filter (hypervisorInventory -> {
                // Get hypervisors where we know the Device resource units match those of the hypervisors
                List<String> requiredResourceClasses = managedDevices.stream()
                    .map(FlavourDevice::getDevicePool)
                    .map(DevicePool::getResourceClass)
                    .toList();

                return hypervisorInventory.hasResourceClasses(requiredResourceClasses);
            })
            .map(hypervisorInventory -> {
                // Get the number of available flavours for each hypervisor
                long vcpusAvailable = hypervisorInventory.cpusAvailable();
                long ramMBAvailable = hypervisorInventory.memoryMBAvailable();
                long totalCPUs = hypervisorInventory.totalCPUs();
                long totalMemoryMB = hypervisorInventory.totalMemoryMB();

                AvailabilityData devicesAvailabilityData = managedDevices.stream()
                    .map(flavourDevice -> {
                        final DevicePool devicePool = flavourDevice.getDevicePool();
                        final String resourceClass = devicePool.getResourceClass();
                        // Get number of units on the hypervisor
                        long hypervisorAvailable = hypervisorInventory.getAvailableResource(resourceClass);
                        long hypervisorTotal = hypervisorInventory.getTotalResource(resourceClass);
                        long availableHypervisorUnits = hypervisorAvailable / flavourDevice.getUnitCount();
                        long totalHypervisorUnits = hypervisorTotal / flavourDevice.getUnitCount();

                        return new AvailabilityData(availableHypervisorUnits, totalHypervisorUnits, flavourUsage);
                    })
                    .reduce(new AvailabilityData(999999L, 999999L, flavourUsage), (acc, curr) -> {
                        return new AvailabilityData(Math.min(acc.available(), curr.available()), Math.min(acc.total(), curr.total()), flavourUsage);
                    });

                long vcpuUnitsAvailable = vcpusAvailable / flavourResourceRequirement.vcpus;
                long ramUnitsAvailable = ramMBAvailable / flavourResourceRequirement.memoryMB;
                long vcpuUnitsTotal = totalCPUs / flavourResourceRequirement.vcpus;
                long ramUnitsTotal = totalMemoryMB / flavourResourceRequirement.memoryMB;

                long unitsAvailable = Math.min(devicesAvailabilityData.available(), Math.min(vcpuUnitsAvailable, ramUnitsAvailable));
                long unitsTotal = Math.min(devicesAvailabilityData.total(), Math.min(vcpuUnitsTotal, ramUnitsTotal));
                return new AvailabilityData(unitsAvailable, unitsTotal, flavourUsage);

            })
            .reduce(new AvailabilityData(0L, 0L, flavourUsage), (acc, cur) -> {
                return new AvailabilityData(acc.available() + cur.available(), acc.total() + cur.total(), flavourUsage);
            });

        // Take into account device pool usage/manual limits
        for (FlavourDevice device : managedDevices) {
            final AvailabilityData devicePoolAvailability = devicePoolUsages.stream()
                .filter(aUsage -> device.getDevicePool().getResourceClass().equals(aUsage.getResourceClass()))
                .map(devicePoolUsage -> {
                    return new AvailabilityData((long)devicePoolUsage.getAvailableUnits(), (long)devicePoolUsage.getTotalUnits(), flavourUsage);
                })
                .findAny()
                .orElse(null);

            // availability goes to the manually specified value if it is lower
            if (devicePoolAvailability != null) {
                long availableUnits = Math.min(availabilityData.available(), devicePoolAvailability.available() / device.getUnitCount());
                long totalUnits = Math.min(availabilityData.total(), devicePoolAvailability.total() / device.getUnitCount());
                availabilityData = new AvailabilityData(availableUnits, totalUnits, flavourUsage);
            }
        }

        if (!unmanagedDevices.isEmpty()) {
            Optional<AvailabilityData> unmanagedDeviceAvailability = this.getDeviceAvailability(unmanagedDevices);
            return this.convertDeviceAvailabilityToFlavourAvailability(flavourResourceRequirement, availabilityData, unmanagedDeviceAvailability.orElse(null));

        } else {
            // This is a definite value obtained directly from the hypervisors, taking into account manually managed device limits
            return new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.of(availabilityData), FlavourAvailability.AvailabilityConfidence.CERTAIN);
        }

    }


    private Optional<AvailabilityData> getDeviceAvailability(final List<FlavourDevice> flavourDevices) {
        return flavourDevices.stream()
            .map(flavourDevice -> {
                final DevicePool devicePool = flavourDevice.getDevicePool();
                final DevicePoolUsage devicePoolUsage = devicePoolUsages.stream()
                    .filter(aDevicePoolUsage -> aDevicePoolUsage.getDevicePoolId().equals(devicePool.getId()))
                    .findFirst()
                    .orElse(null);

                Optional<AvailabilityData> availabilityData;
                if (devicePoolUsage == null) {
                    availabilityData =  Optional.empty();

                } else {
                    if (devicePoolUsage.getAvailableUnits() == null) {
                        availabilityData = Optional.empty();
                    } else {
                        long availability = (long)devicePoolUsage.getAvailableUnits() / (long)flavourDevice.getUnitCount();
                        long total = (long)devicePoolUsage.getTotalUnits() / (long)flavourDevice.getUnitCount();
                        availabilityData = Optional.of(new AvailabilityData(availability, total, total - availability));
                    }
                }
                return availabilityData;
            })
            .reduce(Optional.of(new AvailabilityData(99999L, 99999L, 0L)), (acc, next) -> {
                // If any are zero, return zero, otherwise return if any are unknown, otherwise the minimum of non empty ones
                if (acc.isPresent() && acc.get().available() == 0L) {
                    return acc;
                } else if (next.isPresent() && next.get().available() == 0L) {
                    return next;
                } else if (next.isPresent() && acc.isPresent()) {
                    long available = Math.min(next.get().available(), acc.get().available());
                    long total = Math.min(next.get().total(), acc.get().total());
                    return Optional.of(new AvailabilityData(available, total, total - available));
                } else if (acc.isEmpty()) {
                    return acc;
                } else {
                    return next;
                }
            });
    }

    private FlavourAvailability convertDeviceAvailabilityToFlavourAvailability(final FlavourResourceRequirement flavourResourceRequirement, final AvailabilityData knownFlavourAvailability, final AvailabilityData deviceAvailability) {
        final Long flavourUsage = this.flavourUsages.getFlavourUsage(flavourResourceRequirement.flavour);
        if (deviceAvailability == null) {
            // If we have no knowledge of whether all the devices are available
            logger.debug("Flavour {} (RAM_MB {} vCPUs {}), available Device Units Unknown", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus);
            return new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);

        } else {
            long availableUnits = Math.min(knownFlavourAvailability.available(), deviceAvailability.available());
            long totalUnits = Math.min(knownFlavourAvailability.total(), deviceAvailability.total());
            logger.debug("Flavour {} (RAM_MB {} vCPUs {}), Devices available {}, available Units {}", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus, deviceAvailability.available(), availableUnits);
            return new FlavourAvailability(availabilityDate, flavourResourceRequirement.flavour, Optional.of(new AvailabilityData(availableUnits, totalUnits, flavourUsage)), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
        }
    }

}
