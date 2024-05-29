package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.FlavourType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class FlavourResource {

    private final FlavourService flavourService;

    @Inject
    public FlavourResource(final FlavourService flavourService) {
        this.flavourService = flavourService;
    }

    /**
     * Get a list of flavours
     *
     * @return a list of flavours
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull List<FlavourType> flavours() throws DataFetchingException {
        try {

            return this.flavourService.getAllForAdmin().stream()
            .map(FlavourType::new)
            .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }


    /**
     * Count all flavours
     *
     * @return a count of images
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countFlavours() throws DataFetchingException {
        try {
            return flavourService.countAllForAdmin();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
