package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("NumberInstancesByDevicePool")
public class DevicePoolUsageType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long devicePoolId;
    private final @NotNull String devicePoolName;
    private final @NotNull Integer totalUnits ;
    private final @NotNull Integer usedUnits ;

    public DevicePoolUsageType(final DevicePoolUsage numberInstancesByDevicePool) {
        this.devicePoolId = numberInstancesByDevicePool.getDevicePoolId();
        this.devicePoolName = numberInstancesByDevicePool.getDevicePoolName();
        this.totalUnits = numberInstancesByDevicePool.getTotalUnits();
        this.usedUnits = numberInstancesByDevicePool.getUsedUnits();
    }

    public Long getDevicePoolId() {
        return devicePoolId;
    }

    public String getDevicePoolName() {
        return devicePoolName;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public Integer getUsedUnits() {
        return usedUnits;
    }
}
