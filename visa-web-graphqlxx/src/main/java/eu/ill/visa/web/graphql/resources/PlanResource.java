package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.PlanType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class PlanResource {

    private final PlanService planService;

    @Inject
    public PlanResource(final PlanService planService) {
        this.planService = planService;
    }

    /**
     * Get a list of plans
     *
     * @return a list of plans
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull List<PlanType> plans() throws DataFetchingException {
        try {
            return this.planService.getAllForAdmin().stream()
                .map(PlanType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
