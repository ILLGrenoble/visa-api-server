package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.InstanceJupyterSessionService;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.inputs.OrderByInput;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.inputs.QueryFilterInput;
import eu.ill.visa.web.graphql.relay.Connection;
import eu.ill.visa.web.graphql.relay.PageInfo;
import eu.ill.visa.web.graphql.types.InstanceJupyterSessionType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class InstanceJupyterSessionResource {

    private final InstanceJupyterSessionService instanceJupyterSessionService;

    @Inject
    public InstanceJupyterSessionResource(final InstanceJupyterSessionService instanceJupyterSessionService) {
        this.instanceJupyterSessionService = instanceJupyterSessionService;
    }


    /**
     * Get a list of jupyter sessions
     *
     * @param filter     the given query filter
     * @param orderBy    the ordering of results
     * @param pagination the pagination (limit and offset)
     * @return a list of jupyter sessions
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull Connection<InstanceJupyterSessionType> jupyterSessions(final QueryFilterInput filter, final OrderByInput orderBy, @NotNull PaginationInput pagination) throws DataFetchingException {
        try {
            final List<InstanceJupyterSessionType> results = instanceJupyterSessionService.getAll(
                requireNonNullElseGet(filter.toQueryFilter(), QueryFilter::new),
                requireNonNullElseGet(orderBy.toOrderBy(), () -> new OrderBy("id", true)), pagination.toPagination()
            ).stream()
                .map(InstanceJupyterSessionType::new)
                .toList();
            final PageInfo pageInfo = new PageInfo(instanceJupyterSessionService.countAll(filter.toQueryFilter()), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Count all active jupyter sessions
     *
     * @return a count of active Jupyter sessions
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countJupyterSessions() throws DataFetchingException {
        return instanceJupyterSessionService.countAllInstances();
    }


}
