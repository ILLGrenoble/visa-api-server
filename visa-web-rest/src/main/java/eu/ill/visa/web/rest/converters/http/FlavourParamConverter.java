package eu.ill.visa.web.rest.converters.http;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.core.entity.Flavour;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
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
