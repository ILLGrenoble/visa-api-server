package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.FlavourAvailabilityService;
import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.types.FlavourAvailabilitiesFutureType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;
import java.util.Map;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class FlavourAvailabilityResource {

    private final FlavourAvailabilityService flavourAvailabilityService;
    private final FlavourService flavourService;

    @Inject
    public FlavourAvailabilityResource(final FlavourAvailabilityService flavourAvailabilityService,
                                       final FlavourService flavourService) {
        this.flavourAvailabilityService = flavourAvailabilityService;
        this.flavourService = flavourService;
    }

    @Query
    public @NotNull List<FlavourAvailabilitiesFutureType> flavourAvailabilitiesFutures() {
        Map<Flavour, List<FlavourAvailability>> futureAvailabilities = this.flavourAvailabilityService.getAllFutureAvailabilities();
        return this.flavourService.getAllForAdmin().stream()
            .map(flavour -> new FlavourAvailabilitiesFutureType(flavour, futureAvailabilities.get(flavour)))
            .toList();
    }

}
