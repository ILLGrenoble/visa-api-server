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
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long total ;

    public DevicePoolUsageType(final DevicePoolUsage numberInstancesByDevicePool) {
        this.devicePoolId = numberInstancesByDevicePool.getDevicePoolId();
        this.devicePoolName = numberInstancesByDevicePool.getDevicePoolName();
        this.total = numberInstancesByDevicePool.getTotal();
    }

    public Long getDevicePoolId() {
        return devicePoolId;
    }

    public String getDevicePoolName() {
        return devicePoolName;
    }

    public Long getTotal() {
        return total;
    }

}
