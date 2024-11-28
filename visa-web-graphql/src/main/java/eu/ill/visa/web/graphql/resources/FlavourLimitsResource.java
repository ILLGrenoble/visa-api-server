package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.FlavourLimitService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.types.FlavourLimitType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class FlavourLimitsResource {

    private final FlavourLimitService flavourLimitService;

    @Inject
    public FlavourLimitsResource(final FlavourLimitService flavourLimitService) {
        this.flavourLimitService = flavourLimitService;
    }

    /**
     * Get a list of flavour limits
     *
     * @return a list of flavour limits
     */
    @Query
    public @NotNull List<FlavourLimitType> flavourLimits() {
        return flavourLimitService.getAll().stream().map(FlavourLimitType::new).toList();
    }

}
