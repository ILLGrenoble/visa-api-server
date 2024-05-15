package eu.ill.visa.web.graphqlxx.resources;

import com.github.dozermapper.core.Mapper;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.web.graphqlxx.types.PlanType;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

//@GraphQLApi
public class PlanResource {

    private final PlanService planService;
    private final Mapper mapper;

    public PlanResource(final PlanService planService,
                        final Mapper mapper) {
        this.planService = planService;
        this.mapper = mapper;
    }

    @Query
    public List<PlanType> plans() {
        return this.planService.getAllForAdmin().stream()
            .map(plan -> mapper.map(plan, PlanType.class))
            .toList();
    }

}
