package eu.ill.visa.web.converters.http;


import com.google.inject.Inject;
import eu.ill.visa.business.services.CycleService;
import eu.ill.visa.core.domain.Cycle;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;

public class CycleParamConverter implements ParamConverter<Cycle> {

    private final CycleService cycleService;

    @Inject
    public CycleParamConverter(final CycleService cycleService) {
        this.cycleService = cycleService;
    }

    @Override
    public Cycle fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final Cycle cycle = cycleService.getById(id);
            if (cycle == null) {
                throw new NotFoundException("Cycle not found");
            }
            return cycle;
        }
        throw new NotFoundException("Cycle not found");
    }

    @Override
    public String toString(final Cycle value) {
        return value.toString();
    }
}
