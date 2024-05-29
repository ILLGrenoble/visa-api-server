package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.InstanceSessionMemberService;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.relay.Connection;
import eu.ill.visa.web.graphql.relay.PageInfo;
import eu.ill.visa.web.graphql.types.InstanceSessionMemberType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
    public Connection<InstanceSessionMemberType> sessions(final QueryFilter filter, final OrderBy orderBy, Pagination pagination) throws DataFetchingException {
        try {
            final List<InstanceSessionMemberType> results = instanceSessionMemberService.getAll(
                requireNonNullElseGet(filter, QueryFilter::new),
                requireNonNullElseGet(orderBy, () -> new OrderBy("id", true)), pagination
            ).stream()
                .map(InstanceSessionMemberType::new)
                .toList();
            final PageInfo pageInfo = new PageInfo(instanceSessionMemberService.countAll(filter), pagination.getLimit(), pagination.getOffset());
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
    public @AdaptToScalar(Scalar.Int.class) Long countSessions(final QueryFilter filter) throws DataFetchingException {
        try {
            return instanceSessionMemberService.countAll(requireNonNullElseGet(filter, QueryFilter::new));
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
    public @AdaptToScalar(Scalar.Int.class) Long countActiveSessions(final QueryFilter filter) throws DataFetchingException {
        try {
            return instanceSessionMemberService.countAllActive(requireNonNullElseGet(filter, QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
