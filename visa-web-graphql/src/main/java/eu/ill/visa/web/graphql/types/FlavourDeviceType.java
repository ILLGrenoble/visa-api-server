package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.FlavourDevice;
import org.eclipse.microprofile.graphql.Type;

@Type("FlavourDevice")
public class FlavourDeviceType {

    private final DevicePoolType devicePool;
    private final Integer unitCount;

    public FlavourDeviceType(final FlavourDevice flavourDevice) {
        this.devicePool = new DevicePoolType(flavourDevice.getDevicePool());
        this.unitCount = flavourDevice.getUnitCount();
    }

    public DevicePoolType getDevicePool() {
        return devicePool;
    }

    public Integer getUnitCount() {
        return unitCount;
    }
}
