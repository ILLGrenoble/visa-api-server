package eu.ill.visa.web.converters.http;


import jakarta.inject.Inject;
import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.core.domain.Flavour;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;

public class FlavourParamConverter implements ParamConverter<Flavour> {

    private final FlavourService flavourService;

    @Inject
    public FlavourParamConverter(final FlavourService flavourService) {
        this.flavourService = flavourService;
    }

    @Override
    public Flavour fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final Flavour flavour = flavourService.getById(id);
            if (flavour == null) {
                throw new NotFoundException("Flavour not found");
            }
            return flavour;
        }
        throw new NotFoundException("Flavour not found");

    }

    @Override
    public String toString(final Flavour value) {
        return value.toString();
    }
}
