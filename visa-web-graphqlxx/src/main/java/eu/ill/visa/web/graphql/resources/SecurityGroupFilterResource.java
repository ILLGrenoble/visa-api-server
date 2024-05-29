package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.SecurityGroupFilterService;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.SecurityGroupFilterType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class SecurityGroupFilterResource {

    private final SecurityGroupFilterService securityGroupFilterService;

    @Inject
    public SecurityGroupFilterResource(final SecurityGroupFilterService securityGroupFilterService) {
        this.securityGroupFilterService = securityGroupFilterService;
    }


    /**
     * Get a list of securityGroupFilters
     *
     * @return a list of security group filters
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public List<SecurityGroupFilterType> securityGroupFilters(QueryFilter filter) throws DataFetchingException {
        try {
            return securityGroupFilterService.getAll(
                requireNonNullElseGet(filter, QueryFilter::new), new OrderBy("objectType", true)
            ).stream()
                .map(SecurityGroupFilterType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
