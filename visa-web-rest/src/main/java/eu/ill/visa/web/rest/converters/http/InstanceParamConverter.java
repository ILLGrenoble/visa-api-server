package eu.ill.visa.web.rest.converters.http;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
public class InstanceParamConverter implements ParamConverter<Instance> {

    private final InstanceService instanceService;

    @Inject
    public InstanceParamConverter(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @Override
    public Instance fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final Instance instance = instanceService.getFullById(id);
            if (instance == null) {
                throw new NotFoundException("Instance not found");
            }
            return instance;

        } else if (value.matches("[a-zA-Z0-9]+")) {
            final Instance instance = instanceService.getFullByUID(value);
            if (instance == null) {
                throw new NotFoundException("Instance not found");
            }
            return instance;
        }
        throw new NotFoundException("Instance not found");
    }

    @Override
    public String toString(final Instance value) {
        return value.toString();
    }
}
