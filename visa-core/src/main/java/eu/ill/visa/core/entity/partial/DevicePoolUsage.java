package eu.ill.visa.core.entity.partial;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DevicePoolUsage {
    private Long devicePoolId;
    private String devicePoolName;
    private Long total ;

    public DevicePoolUsage(final Long devicePoolId, final String devicePoolName, final Long total) {
        this.devicePoolId = devicePoolId;
        this.devicePoolName = devicePoolName;
        this.total = total;
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

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }


}
