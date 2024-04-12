package eu.ill.visa.web.converters.http;

import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.core.domain.Experiment;

import jakarta.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;

public class ExperimentParamConverter implements ParamConverter<Experiment> {

    private final ExperimentService experimentService;

    @Inject
    public ExperimentParamConverter(final ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @Override
    public Experiment fromString(final String id) {
        final Experiment experiment = experimentService.getById(id);
        if (experiment == null) {
            throw new NotFoundException("Experiment not found");
        }
        return experiment;
    }

    @Override
    public String toString(final Experiment value) {
        return value.toString();
    }
}
