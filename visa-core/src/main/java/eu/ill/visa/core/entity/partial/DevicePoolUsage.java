package eu.ill.visa.core.entity.partial;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DevicePoolUsage {
    private final Long devicePoolId;
    private final Long cloudId;
    private final String devicePoolName;
    private final String resourceClass;
    private final Integer usedUnits ;
    private final Integer totalUnits ;

    public DevicePoolUsage(final Long devicePoolId, final Long cloudId, final String devicePoolName, final String resourceClass, final long totalUnits, final long usedUnits) {
        this.devicePoolId = devicePoolId;
        this.cloudId = cloudId;
        this.devicePoolName = devicePoolName;
        this.resourceClass = resourceClass;
        this.totalUnits = (int)totalUnits;
        this.usedUnits = (int)usedUnits;
    }

    public Long getDevicePoolId() {
        return devicePoolId;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public String getDevicePoolName() {
        return devicePoolName;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public Integer getUsedUnits() {
        return usedUnits;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }
}
