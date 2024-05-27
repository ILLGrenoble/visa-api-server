package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Flavour;

public class FlavourDto {

    private final Long id;
    private final String name;
    private final Integer memory;
    private final Float cpu;

    public FlavourDto(final Flavour flavour) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
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
}
