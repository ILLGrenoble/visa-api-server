package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public record SystemResources(Long cloudId, Date availabilityDate, CloudResources cloudResources, List<DevicePoolUsage> devicePoolUsages, List<HypervisorInventory> hypervisorInventories) {
    private static final Logger logger = LoggerFactory.getLogger(SystemResources.class);

    private record FlavourResourceRequirement(Long cloudId, Flavour flavour,  Long vcpus, Long memoryMB, List<FlavourDevice> flavourDevices) {
        boolean hasDevices() {
            return !this.flavourDevices.isEmpty();
        }
    }

    public SystemResources(Long cloudId, CloudResources cloudResources, List<DevicePoolUsage> devicePoolUsages,  List<HypervisorInventory> hypervisorInventories) {
        this(cloudId, new Date(), cloudResources, devicePoolUsages, hypervisorInventories);
    }

    public SystemResources onInstanceDeleted(final Instance instance, final Long hostHypervisorId) {
        Date date = instance.getTerminationDate();

        CloudResources cloudResources = this.cloudResources.onInstanceReleased(instance);

        List<DevicePoolUsage> devicePoolUsages = this.devicePoolUsages;
        for (InstanceDeviceAllocation instanceDeviceAllocation : instance.getDeviceAllocations()) {
            final DevicePool devicePool = instanceDeviceAllocation.getDevicePool();
            DevicePoolUsage devicePoolUsage = devicePoolUsages.stream()
                .filter(usage -> usage.getDevicePoolId().equals(devicePool.getId()))
                .findFirst()
                .orElse(new DevicePoolUsage(devicePool.getId(), this.cloudId, devicePool.getName(), devicePool.getResourceClass(), -1, 0))
                .onUnitsReleased(instanceDeviceAllocation.getUnitCount());
        }

        List<HypervisorInventory> hypervisorInventories = this.hypervisorInventories.stream()
            .map(inventory -> {
                if (inventory.hypervisorId().equals(hostHypervisorId)) {
                    return inventory.onInstanceReleased(instance);
                } else {
                    return inventory;
                }

            }).toList();

        return new SystemResources(cloudId, date, cloudResources, devicePoolUsages, hypervisorInventories);
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

        FlavourAvailability availability = null;
        if (hypervisorInventories != null && !hypervisorInventories.isEmpty()) {
            // Determine total number of units possible
            long totalAvailableUnits = hypervisorInventories.stream()
                .map(hypervisorInventory -> {
                    long vcpusAvailable = hypervisorInventory.cpusAvailable();
                    long ramMBAvailable = hypervisorInventory.memoryMBAvailable();

                    long vcpuUnits = vcpusAvailable / flavourResourceRequirement.vcpus;
                    long ramUnits = ramMBAvailable / flavourResourceRequirement.memoryMB;


                    long hypervisorAvailableUnits = Math.min(vcpuUnits, ramUnits);

                    logger.debug("Flavour {} (RAM_MB {} vCPUs {}), hypervisor {} RAM_MB {} vCPUs {}, available Units {}", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus, hypervisorInventory.hostname(), ramMBAvailable, vcpusAvailable, hypervisorAvailableUnits);

                    return hypervisorAvailableUnits;

                })
                .mapToLong(Long::longValue)
                .sum();

            availability = new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(totalAvailableUnits), FlavourAvailability.AvailabilityConfidence.CERTAIN);
        }

        if (cloudResources != null) {
            long availableUnits = this.getAvailableUnitsFromCloudResources(flavourResourceRequirement);
            logger.debug("Flavour {} (RAM_MB {} vCPUs {}), available Units {}", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus, availableUnits);

            if (availability != null && availability.availableUnits().isPresent()) {
                long minUnits = Math.min(availableUnits, availability.availableUnits().get());
                availability = new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(minUnits), FlavourAvailability.AvailabilityConfidence.CERTAIN);

            } else {
                availability = new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(availableUnits), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
            }
        }

        if (availability == null) {
            // If we have no hypervisor information nor cloud limit information then we know nothing about the availability
            availability = new FlavourAvailability(flavourResourceRequirement.flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
        }

        return availability;
    }


    private FlavourAvailability getAvailabilityForDeviceFlavour(final FlavourResourceRequirement flavourResourceRequirement) {

        FlavourAvailability availability = null;
        if (hypervisorInventories != null && !hypervisorInventories.isEmpty()) {
            availability = this.getDeviceFlavourAvailabilityWithHypervisor(flavourResourceRequirement);
        }

        if (cloudResources != null) {
            long availableUnits = this.getAvailableUnitsFromCloudResources(flavourResourceRequirement);

            if (availability != null && availability.availableUnits().isPresent()) {
                // Make sure cloud limits are taken into account even when hypervisor data in known
                long minUnits = Math.min(availableUnits, availability.availableUnits().get());
                availability = new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(minUnits), FlavourAvailability.AvailabilityConfidence.CERTAIN);

            } else {
                if (availableUnits > 0) {
                    // Check device availability from
                    Optional<Long> deviceAvailability = this.getDeviceAvailability(flavourResourceRequirement.flavourDevices);
                    availability = this.convertDeviceAvailabilityToFlavourAvailability(flavourResourceRequirement, availableUnits, deviceAvailability);

                } else {
                    logger.debug("Flavour {} (RAM_MB {} vCPUs {}), available Units 0", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus);
                    availability = new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(0L), FlavourAvailability.AvailabilityConfidence.CERTAIN);
                }
            }
        }

        if (availability == null) {
            // If we have no hypervisor information nor cloud limit information then we know nothing about the availability
            availability = new FlavourAvailability(flavourResourceRequirement.flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
        }

        return availability;
    }


    private long getAvailableUnitsFromCloudResources(final FlavourResourceRequirement flavourResourceRequirement) {
        long cloudVCPUsAvailable = cloudResources.getVcpuAvailable();
        long cloudMemoryMBAvailable = cloudResources.getMemoryMBAvailable();

        long vcpuUnits = cloudVCPUsAvailable / flavourResourceRequirement.vcpus;
        long ramUnits = cloudMemoryMBAvailable / flavourResourceRequirement.memoryMB;
        long instanceUnits = cloudResources.getInstancesAvailable();

        return Math.min(instanceUnits, Math.min(vcpuUnits, ramUnits));
    }


    private FlavourAvailability getDeviceFlavourAvailabilityWithHypervisor(final FlavourResourceRequirement flavourResourceRequirement) {
        List<FlavourDevice> managedDevices = flavourResourceRequirement.flavourDevices.stream()
            .filter(flavourDevice -> flavourDevice.getDevicePool().getResourceClass() != null)
            .toList();

        List<FlavourDevice> unmanagedDevices = flavourResourceRequirement.flavourDevices.stream()
            .filter(flavourDevice -> flavourDevice.getDevicePool().getResourceClass() == null)
            .toList();


        long availableUnits = hypervisorInventories.stream()
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
                long devicesAvailable = managedDevices.stream()
                    .map(flavourDevice -> {
                        final DevicePool devicePool = flavourDevice.getDevicePool();
                        final String resourceClass = devicePool.getResourceClass();
                        // Get number of units on the hypervisor
                        long hypervisorAvailability = hypervisorInventory.getAvailableResource(resourceClass);

                        return hypervisorAvailability / flavourDevice.getUnitCount();
                    })
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0L);

                long vcpuUnits = vcpusAvailable / flavourResourceRequirement.vcpus;
                long ramUnits = ramMBAvailable / flavourResourceRequirement.memoryMB;

                return Math.min(devicesAvailable, Math.min(vcpuUnits, ramUnits));
            })
            .mapToLong(Long::longValue)
            .sum();

        // Take into account device pool usage/manual limits
        for (FlavourDevice device : managedDevices) {
            final Integer available = devicePoolUsages.stream()
                .filter(aUsage -> device.getDevicePool().getResourceClass().equals(aUsage.getResourceClass()))
                .map(DevicePoolUsage::getAvailableUnits)
                .findAny()
                .orElse(null);

            // availability goes to the manually specified value if it is lower
            if (available != null) {
                availableUnits = Math.min(availableUnits, available / device.getUnitCount());
            }
        }

        if (!unmanagedDevices.isEmpty()) {
            Optional<Long> unmanagedDeviceAvailability = this.getDeviceAvailability(unmanagedDevices);
            return this.convertDeviceAvailabilityToFlavourAvailability(flavourResourceRequirement, availableUnits, unmanagedDeviceAvailability);

        } else {
            // This is a definite value obtained directly from the hypervisors, taking into account manually managed device limits
            return new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(availableUnits), FlavourAvailability.AvailabilityConfidence.CERTAIN);
        }

    }


    private Optional<Long> getDeviceAvailability(final List<FlavourDevice> flavourDevices) {
        return flavourDevices.stream()
            .map(flavourDevice -> {
                final DevicePool devicePool = flavourDevice.getDevicePool();
                Optional<Long> availability;
                final DevicePoolUsage devicePoolUsage = devicePoolUsages.stream()
                    .filter(aDevicePoolUsage -> aDevicePoolUsage.getDevicePoolId().equals(devicePool.getId()))
                    .findFirst()
                    .orElse(null);

                if (devicePoolUsage == null) {
                    availability = Optional.empty();

                } else {
                    if (devicePoolUsage.getAvailableUnits() == null) {
                        availability = Optional.empty();
                    } else {
                        availability = Optional.of(((long)devicePoolUsage.getAvailableUnits() / (long)flavourDevice.getUnitCount()));
                    }
                }
                return availability;
            })
            .reduce(Optional.of(99999L), (acc, next) -> {
                // If any are zero, return zero, otherwise return if any are unknown, otherwise the minimum of non empty ones
                if (acc.isPresent() && acc.get() == 0L) {
                    return acc;
                } else if (next.isPresent() && next.get() == 0L) {
                    return next;
                } else if (next.isPresent() && acc.isPresent()) {
                    return next.get() < acc.get() ? next : acc;
                } else if (acc.isEmpty()) {
                    return acc;
                } else {
                    return next;
                }
            });
    }

    private FlavourAvailability convertDeviceAvailabilityToFlavourAvailability(final FlavourResourceRequirement flavourResourceRequirement, final Long knownFlavourAvailability, final Optional<Long> deviceAvailability) {
        if (deviceAvailability.isEmpty()) {
            // If we have no knowledge of whether all the devices are available
            logger.debug("Flavour {} (RAM_MB {} vCPUs {}), available Device Units Unknown", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus);
            return new FlavourAvailability(flavourResourceRequirement.flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);

        } else {
            long devicesAvailable = deviceAvailability.get();
            if (devicesAvailable == 0) {
                logger.debug("Flavour {} (RAM_MB {} vCPUs {}), available Device Units 0", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus);
                return new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(0L), FlavourAvailability.AvailabilityConfidence.CERTAIN);

            } else {
                long availableUnits = Math.min(knownFlavourAvailability, devicesAvailable);
                logger.debug("Flavour {} (RAM_MB {} vCPUs {}), Devices available {}, available Units {}", flavourResourceRequirement.flavour.getName(), flavourResourceRequirement.memoryMB, flavourResourceRequirement.vcpus, devicesAvailable, availableUnits);
                return new FlavourAvailability(flavourResourceRequirement.flavour, Optional.of(availableUnits), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);
            }
        }
    }

}
