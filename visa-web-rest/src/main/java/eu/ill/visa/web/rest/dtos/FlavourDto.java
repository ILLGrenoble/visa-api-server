package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.FlavourDevice;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FlavourDto {
    private final Logger logger = LoggerFactory.getLogger(FlavourDto.class);

    private final Long id;
    private final String name;
    private final Integer memory;
    private final Float cpu;
    private final List<FlavourDeviceDto> devices;
    private final Boolean isAvailable;

    public FlavourDto(final Flavour flavour) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
        this.devices = flavour.getDevices().stream().map(FlavourDeviceDto::new).toList();
        this.isAvailable = true;
    }

    public FlavourDto(final Flavour flavour, final List<DevicePoolUsage> devicePoolUsage) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
        this.devices = flavour.getDevices().stream().map(FlavourDeviceDto::new).toList();
        this.isAvailable = flavour.getDevices().stream().map(FlavourDevice::getDevicePool).allMatch(devicePool -> {
            boolean deviceIsAvailable = devicePoolUsage.stream().filter(usage -> usage.getDevicePoolId().equals(devicePool.getId())).map(usage -> usage.getUsedUnits() < devicePool.getTotalUnits()).findAny().orElse(true);
            if (!deviceIsAvailable) {
                logger.debug("device pool {} ({}) is not available", devicePool.getId(), devicePool.getName());
            }
            return false;
//            return deviceIsAvailable;
        });
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getMemory() {
        return memory;
    }

    public Float getCpu() {
        return cpu;
    }

    public List<FlavourDeviceDto> getDevices() {
        return devices;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }
}
