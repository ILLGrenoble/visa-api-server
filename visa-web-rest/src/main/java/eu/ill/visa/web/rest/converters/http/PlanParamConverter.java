package eu.ill.visa.web.rest.converters.http;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.core.entity.Plan;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
public class PlanParamConverter implements ParamConverter<Plan> {

    private final PlanService planService;

    @Inject
    public PlanParamConverter(final PlanService planService) {
        this.planService = planService;
    }

    @Override
    public Plan fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final Plan plan = planService.getById(id);
            if (plan == null) {
                throw new NotFoundException("Plan not found");
            }
            return plan;
        }
        throw new NotFoundException("Plan not found");


    }

    @Override
    public String toString(final Plan value) {
        return value.toString();
    }
}
