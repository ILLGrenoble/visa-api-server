package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.FlavourDevice;

public class FlavourDeviceDto {

    private final DevicePoolDto devicePool;
    private final Integer unitCount;

    public FlavourDeviceDto(final FlavourDevice flavourDevice) {
        this.devicePool = new DevicePoolDto(flavourDevice.getDevicePool());
        this.unitCount = flavourDevice.getUnitCount();
    }

    public DevicePoolDto getDevicePool() {
        return devicePool;
    }

    public Integer getUnitCount() {
        return unitCount;
    }
}
