package eu.ill.visa.web.rest.converters;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.core.entity.Instrument;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
public class InstrumentParamConverter implements ParamConverter<Instrument> {

    private final InstrumentService instrumentService;

    @Inject
    public InstrumentParamConverter(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Override
    public Instrument fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final Instrument instrument = instrumentService.getById(id);
            if (instrument == null) {
                throw new NotFoundException("Instrument not found");
            }
            return instrument;
        }
        throw new NotFoundException("Instrument not found");
    }

    @Override
    public String toString(final Instrument value) {
        return value.toString();
    }
}
