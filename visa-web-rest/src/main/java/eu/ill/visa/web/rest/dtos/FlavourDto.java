package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.entity.Flavour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class FlavourDto {
    private final static Logger logger = LoggerFactory.getLogger(FlavourDto.class);

    private final Long id;
    private final String name;
    private final Integer memory;
    private final Float cpu;
    private final List<FlavourDeviceDto> devices;
    private final Boolean isAvailable;
    private final Long lifetimeMinutes;

    public FlavourDto(final Flavour flavour) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
        this.devices = flavour.getDevices().stream().map(FlavourDeviceDto::new).toList();
        this.isAvailable = true;
        this.lifetimeMinutes = null;
    }

    public FlavourDto(final Flavour flavour, final FlavourAvailability flavourAvailability, final Duration lifetimeDuration) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
        this.devices = flavour.getDevices().stream().map(FlavourDeviceDto::new).toList();
        this.isAvailable = flavourAvailability == null || !flavourAvailability.isAvailable().equals(FlavourAvailability.AvailabilityState.NO);
        this.lifetimeMinutes = lifetimeDuration.toMinutes();
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

    public Long getLifetimeMinutes() {
        return lifetimeMinutes;
    }
}
