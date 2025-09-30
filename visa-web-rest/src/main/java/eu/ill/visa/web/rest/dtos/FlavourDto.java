package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Flavour;

import java.util.List;

public class FlavourDto {

    private final Long id;
    private final String name;
    private final Integer memory;
    private final Float cpu;
    private final List<FlavourDeviceDto> devices;

    public FlavourDto(final Flavour flavour) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
        this.devices = flavour.getDevices().stream().map(FlavourDeviceDto::new).toList();
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
}
