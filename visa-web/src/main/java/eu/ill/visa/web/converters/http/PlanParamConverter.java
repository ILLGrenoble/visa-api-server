package eu.ill.visa.web.converters.http;


import com.google.inject.Inject;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.core.domain.Plan;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;

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
