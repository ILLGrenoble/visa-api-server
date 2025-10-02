package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceDeviceAllocation;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("InstanceDeviceAllocation")
public class InstanceDeviceAllocationType {

    private final @NotNull InstanceType instance;
    private final @NotNull DevicePoolType devicePool;
    private final @NotNull Integer unitCount;

    public InstanceDeviceAllocationType(final InstanceDeviceAllocation instanceDeviceAllocation) {
        this.instance = new InstanceType(instanceDeviceAllocation.getInstance());
        this.devicePool = new DevicePoolType(instanceDeviceAllocation.getDevicePool());
        this.unitCount = instanceDeviceAllocation.getUnitCount();
    }

    public InstanceType getInstance() {
        return instance;
    }

    public DevicePoolType getDevicePool() {
        return devicePool;
    }

    public Integer getUnitCount() {
        return unitCount;
    }
}
