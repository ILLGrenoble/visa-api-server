package eu.ill.visa.web.converters.http;


import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Instance;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;


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
            final Instance instance = instanceService.getById(id);
            if (instance == null) {
                throw new NotFoundException("Instance not found");
            }
            return instance;

        } else if (value.matches("[a-zA-Z0-9]+")) {
            final Instance instance = instanceService.getByUID(value);
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
