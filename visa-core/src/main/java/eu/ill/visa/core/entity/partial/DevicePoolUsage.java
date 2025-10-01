package eu.ill.visa.core.entity.partial;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DevicePoolUsage {
    private Long devicePoolId;
    private String devicePoolName;
    private Integer usedUnits ;
    private Integer totalUnits ;

    public DevicePoolUsage(final Long devicePoolId, final String devicePoolName, final long totalUnits, final long usedUnits) {
        this.devicePoolId = devicePoolId;
        this.devicePoolName = devicePoolName;
        this.totalUnits = (int)totalUnits;
        this.usedUnits = (int)usedUnits;
    }

    public Long getDevicePoolId() {
        return devicePoolId;
    }

    public void setDevicePoolId(Long devicePoolId) {
        this.devicePoolId = devicePoolId;
    }

    public String getDevicePoolName() {
        return devicePoolName;
    }

    public void setDevicePoolName(String devicePoolName) {
        this.devicePoolName = devicePoolName;
    }

    public Integer getUsedUnits() {
        return usedUnits;
    }

    public void setUsedUnits(Integer usedUnits) {
        this.usedUnits = usedUnits;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
    }
}
