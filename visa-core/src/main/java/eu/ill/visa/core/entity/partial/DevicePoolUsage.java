package eu.ill.visa.core.entity.partial;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DevicePoolUsage {
    private final Long devicePoolId;
    private final Long cloudId;
    private final String devicePoolName;
    private final Integer usedUnits ;
    private final Integer totalUnits ;

    public DevicePoolUsage(final Long devicePoolId, final Long cloudId, final String devicePoolName, final long totalUnits, final long usedUnits) {
        this.devicePoolId = devicePoolId;
        this.cloudId = cloudId;
        this.devicePoolName = devicePoolName;
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

    public Integer getUsedUnits() {
        return usedUnits;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }
}
