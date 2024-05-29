package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.inputs.OrderByInput;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.inputs.QueryFilterInput;
import eu.ill.visa.web.graphql.types.Connection;
import eu.ill.visa.web.graphql.types.PageInfo;
import eu.ill.visa.web.graphql.types.UserType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class UserResource {

    private final UserService userService;

    @Inject
    public UserResource(final UserService userService) {
        this.userService = userService;
    }


    /**
     * Get a list of users
     *
     * @param filter     the given query filter
     * @param orderBy    the ordering of results
     * @param pagination the pagination (limit and offset)
     * @return a list of users
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull Connection<UserType> users(final QueryFilterInput filter, final OrderByInput orderBy, @NotNull PaginationInput pagination) throws DataFetchingException {
        try {
            if (!pagination.isLimitBetween(0, 200)) {
                throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
            }
            final List<UserType> results = userService.getAll(
                requireNonNullElseGet(filter.toQueryFilter(), QueryFilter::new),
                requireNonNullElseGet(orderBy.toOrderBy(), () -> new OrderBy("activatedAt", false)), pagination.toPagination()
            ).stream()
                .map(UserType::new)
                .toList();
            final PageInfo pageInfo = new PageInfo(userService.countAll(filter.toQueryFilter()), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get a list of the last seen users
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of recently active users
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull Connection<UserType> recentActiveUsers(@NotNull final PaginationInput pagination) throws DataFetchingException {
        final QueryFilterInput filter = new QueryFilterInput("lastSeenAt IS NOT NULL AND activatedAt IS NOT NULL");
        return users(filter, new OrderByInput("lastSeenAt", false), pagination);
    }

    @Query
    public @NotNull Connection<UserType> searchForUserByLastName(@NotNull final String lastName, @NotNull Boolean onlyActivatedUsers, @NotNull final PaginationInput pagination) throws DataFetchingException {
        try {
            if (!pagination.isLimitBetween(0, 200)) {
                throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
            }
            final List<UserType> results = userService.getAllLikeLastName(lastName, onlyActivatedUsers, pagination.toPagination()).stream()
                .map(UserType::new)
                .toList();
            final PageInfo pageInfo = new PageInfo(userService.countAllLikeLastName(lastName, onlyActivatedUsers), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }


    /**
     * Count all users
     *
     * @param filter a filter to filter the results
     * @return a count of users
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countUsers(final QueryFilterInput filter) throws DataFetchingException {
        try {
            return userService.countAll(requireNonNullElseGet(filter.toQueryFilter(), QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countActivatedUsers() throws DataFetchingException {
        try {
            return userService.countAllActivated();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get a user by a given user identifier
     *
     * @param id the user id
     * @return the user
     * @throws EntityNotFoundException thrown if the user does not exist
     */
    @Query
    public UserType user(final @NotNull String id) throws EntityNotFoundException {
        final User user = userService.getById(id);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }
        return new UserType(user);
    }


}
