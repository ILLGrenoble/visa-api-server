package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public record ResourceUsageModifier(Date modificationDate, Long cloudId, String computeId, long instanceModifier, long cpuModifier, long memoryModifier, List<DeviceResourceUsageModifier> deviceResourceModifiers) {
    public record DeviceResourceUsageModifier(long devicePoolId, String resourceClass, long modifier) {
        public DeviceResourceUsageModifier invert() {
            return new DeviceResourceUsageModifier(devicePoolId, resourceClass, -modifier);
        }
    }

    public ResourceUsageModifier combine(ResourceUsageModifier other) {
        if (other == null) {
            return this;
        }

        List<DeviceResourceUsageModifier> others = new ArrayList<>(other.deviceResourceModifiers);
        List<DeviceResourceUsageModifier> combinedModifiers = deviceResourceModifiers.stream()
            .map(m -> {
                DeviceResourceUsageModifier toCombine = others.stream().filter(o -> o.devicePoolId == m.devicePoolId && o.resourceClass.equals(m.resourceClass)).findFirst().orElse(null);
                if (toCombine != null) {
                    others.remove(toCombine);
                    return new DeviceResourceUsageModifier(m.devicePoolId, m.resourceClass, m.modifier + toCombine.modifier);

                } else {
                    return new DeviceResourceUsageModifier(m.devicePoolId, m.resourceClass, m.modifier);
                }
            })
            .collect(Collectors.toList());
        combinedModifiers.addAll(others);
        return new ResourceUsageModifier(modificationDate, cloudId, null, this.instanceModifier + other.instanceModifier, this.cpuModifier + other.cpuModifier, this.memoryModifier + other.memoryModifier, combinedModifiers);
    }

    public static ResourceUsageModifier fromBookingTokenStart(final BookingToken bookingToken) {
        final BookingRequest bookingRequest = bookingToken.getBookingRequest();
        final Flavour flavour = bookingToken.getFlavour();
        long cpuModifier = flavour.getCpu().longValue();
        long memoryModifier = flavour.getMemory().longValue();
        List<DeviceResourceUsageModifier> deviceResourceModifiers = flavour.getDevices().stream()
            .map(flavourDevice -> {
                final DevicePool devicePool = flavourDevice.getDevicePool();
                return new DeviceResourceUsageModifier(devicePool.getId(), devicePool.getResourceClass(), flavourDevice.getUnitCount());
            })
            .toList();
        long cloudId = flavour.getCloudId() == null ? -1 : flavour.getCloudId();
        Date now = new Date();
        Date date = now.after(bookingRequest.getStartDate()) ? now : bookingRequest.getStartDate();
        return new ResourceUsageModifier(date, cloudId, null, 1, cpuModifier, memoryModifier, deviceResourceModifiers);
    }

    public static ResourceUsageModifier fromBookingTokenEnd(final BookingToken bookingToken) {
        final BookingRequest bookingRequest = bookingToken.getBookingRequest();
        final Flavour flavour = bookingToken.getFlavour();
        long cpuModifier = flavour.getCpu().longValue();
        long memoryModifier = flavour.getMemory().longValue();
        List<DeviceResourceUsageModifier> deviceResourceModifiers = flavour.getDevices().stream()
            .map(flavourDevice -> {
                final DevicePool devicePool = flavourDevice.getDevicePool();
                return new DeviceResourceUsageModifier(devicePool.getId(), devicePool.getResourceClass(), -flavourDevice.getUnitCount());
            })
            .toList();
        long cloudId = flavour.getCloudId() == null ? -1 : flavour.getCloudId();
        return new ResourceUsageModifier(bookingRequest.getDayAfterEndDate(), cloudId, null, -1, -cpuModifier, -memoryModifier, deviceResourceModifiers);
    }

    public static ResourceUsageModifier fromInstanceTermination(final Instance instance) {
        final Flavour flavour = instance.getPlan().getFlavour();
        long cpuModifier = -flavour.getCpu().longValue();
        long memoryModifier = -flavour.getMemory().longValue();
        List<DeviceResourceUsageModifier> deviceResourceModifiers = instance.getDeviceAllocations().stream()
            .map(instanceDeviceAllocation -> {
                final DevicePool devicePool = instanceDeviceAllocation.getDevicePool();
                return new DeviceResourceUsageModifier(devicePool.getId(), devicePool.getResourceClass(), -instanceDeviceAllocation.getUnitCount());
            })
            .toList();
        long cloudId = flavour.getCloudId() == null ? -1 : flavour.getCloudId();

        return new ResourceUsageModifier(instance.getTerminationDate(), cloudId, instance.getComputeId(), -1, cpuModifier, memoryModifier, deviceResourceModifiers);
    }
}
