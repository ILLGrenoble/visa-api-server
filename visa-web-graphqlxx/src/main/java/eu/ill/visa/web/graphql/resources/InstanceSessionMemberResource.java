package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.InstanceSessionMemberService;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.inputs.OrderByInput;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.inputs.QueryFilterInput;
import eu.ill.visa.web.graphql.relay.Connection;
import eu.ill.visa.web.graphql.relay.PageInfo;
import eu.ill.visa.web.graphql.types.InstanceSessionMemberType;
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
public class InstanceSessionMemberResource {

    private final InstanceSessionMemberService instanceSessionMemberService;

    @Inject
    public InstanceSessionMemberResource(final InstanceSessionMemberService instanceSessionMemberService) {
        this.instanceSessionMemberService = instanceSessionMemberService;
    }


    /**
     * Get a list of sessions
     *
     * @param filter     the given query filter
     * @param orderBy    the ordering of results
     * @param pagination the pagination (limit and offset)
     * @return a list of sessions
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull Connection<InstanceSessionMemberType> sessions(final QueryFilterInput filter, final OrderByInput orderBy, @NotNull PaginationInput pagination) throws DataFetchingException {
        try {
            final List<InstanceSessionMemberType> results = instanceSessionMemberService.getAll(
                requireNonNullElseGet(filter.toQueryFilter(), QueryFilter::new),
                requireNonNullElseGet(orderBy.toOrderBy(), () -> new OrderBy("id", true)), pagination.toPagination()
            ).stream()
                .map(InstanceSessionMemberType::new)
                .toList();
            final PageInfo pageInfo = new PageInfo(instanceSessionMemberService.countAll(filter.toQueryFilter()), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Count all sessions
     *
     * @param filter a filter to filter the results
     * @return a count of sessions
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countSessions(final QueryFilterInput filter) throws DataFetchingException {
        try {
            return instanceSessionMemberService.countAll(requireNonNullElseGet(filter.toQueryFilter(), QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Count all active sessions (interaction within the last 5 minutes)
     *
     * @param filter a filter to filter the results
     * @return a count of active sessions
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countActiveSessions(final QueryFilterInput filter) throws DataFetchingException {
        try {
            return instanceSessionMemberService.countAllActive(requireNonNullElseGet(filter.toQueryFilter(), QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
