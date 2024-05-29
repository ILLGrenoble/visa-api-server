package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.types.InstrumentType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class InstrumentResource {

    private final InstrumentService instrumentService;

    @Inject
    public InstrumentResource(final InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    /**
     * Get a list of all instruments
     *
     * @return the list of instruments ordered by name
     */
    @Query
    public @NotNull List<InstrumentType> instruments() {
        return this.instrumentService.getAll().stream().map(InstrumentType::new).toList();
    }

}
