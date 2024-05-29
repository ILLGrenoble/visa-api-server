package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.SecurityGroupService;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.SecurityGroupType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class SecurityGroupResource {

    private final SecurityGroupService securityGroupService;

    @Inject
    public SecurityGroupResource(final SecurityGroupService securityGroupService) {
        this.securityGroupService = securityGroupService;
    }

    /**
     * Get a list of securityGroups
     *
     * @return a list of securityGroups
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public List<SecurityGroupType> securityGroups(QueryFilter filter) throws DataFetchingException {
        try {
            return securityGroupService.getAll(requireNonNullElseGet(filter, QueryFilter::new), new OrderBy("name", true)).stream()
                .map(SecurityGroupType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
