package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Instrument;

public class InstrumentDto {
    private final Long id;
    private final String name;

    public InstrumentDto(final Instrument instrument) {
        this.id = instrument.getId();
        this.name = instrument.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
