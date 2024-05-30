package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.RoleService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.ValidationException;
import eu.ill.visa.web.graphql.inputs.OrderByInput;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.inputs.QueryFilterInput;
import eu.ill.visa.web.graphql.inputs.UserInput;
import eu.ill.visa.web.graphql.types.Connection;
import eu.ill.visa.web.graphql.types.PageInfo;
import eu.ill.visa.web.graphql.types.UserType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static eu.ill.visa.web.graphql.inputs.OrderByInput.toOrderBy;
import static eu.ill.visa.web.graphql.inputs.PaginationInput.toPagination;
import static eu.ill.visa.web.graphql.inputs.QueryFilterInput.toQueryFilter;
import static java.lang.String.format;
import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class UserResource {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final UserService userService;
    private final RoleService roleService;

    @Inject
    public UserResource(final UserService userService,
                        final RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
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
                requireNonNullElseGet(toQueryFilter(filter), QueryFilter::new),
                requireNonNullElseGet(toOrderBy(orderBy), () -> new OrderBy("activatedAt", false)), toPagination(pagination)
            ).stream()
                .map(UserType::new)
                .toList();
            final PageInfo pageInfo = new PageInfo(userService.countAll(toQueryFilter(filter)), pagination.getLimit(), pagination.getOffset());
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
            final List<UserType> results = userService.getAllLikeLastName(lastName, onlyActivatedUsers, toPagination(pagination)).stream()
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
            return userService.countAll(requireNonNullElseGet(toQueryFilter(filter), QueryFilter::new));
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


    /**
     * Updates a user's role
     *
     * @param userId    the user ID
     * @param roleName  the role name
     * @param isEnabled if the role is to be added or not
     * @return the user
     * @throws EntityNotFoundException thrown if the user or role has not been found
     */
    @Mutation
    public @NotNull UserType updateUserRole(@NotNull String userId, @NotNull String roleName, @NotNull Boolean isEnabled) throws EntityNotFoundException {

        final User user = userService.getById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found for the given user id");
        }
        final Role role = roleService.getByName(roleName);
        if (role == null) {
            throw new EntityNotFoundException("Role not found for the given role name");
        }

        if (isEnabled) {
            user.addRole(role);
        } else {
            user.removeRole(role);
        }

        this.userService.save(user);

        return new UserType(user);
    }

    /**
     * Update a user
     *
     * @param id the user id to update
     * @param input the user properties to update
     * @return the updated user
     * @throws EntityNotFoundException thrown if the applicationCredential has not been found
     */
    @Mutation
    public @NotNull UserType updateUser(@NotNull String id, @NotNull @Valid UserInput input) throws EntityNotFoundException {
        final User user = userService.getById(id);
        if (user == null) {
            throw new EntityNotFoundException("User not found for the given user id");
        }

        final Role adminRole = roleService.getByName("ADMIN");
        final Role guestRole = roleService.getByName("GUEST");

        // Filter input groups, remove any that do not exist
        List<Role> inputGroups = input.getGroupIds().stream().map(roleService::getById).filter(Objects::nonNull).toList();

        // Get all current groups of the user
        List<Role> userGroups = user.getGroups();

        // Determine which ones to add and to remove
        List<Role> rolesToAdd = inputGroups.stream().filter(role -> !userGroups.contains(role)).toList();
        List<Role> rolesToRemove =  userGroups.stream().filter(userRole -> !inputGroups.contains(userRole)).toList();

        try {
            if (input.getAdmin()) {
                user.addRole(adminRole);
            } else {
                user.removeRole(adminRole);
            }

            if (input.getGuest()) {
                Date guestExpiresAt = input.getGuestExpiresAt() == null ? null : DATE_FORMAT.parse(input.getGuestExpiresAt());
                user.addRole(guestRole, guestExpiresAt);
            } else {
                user.removeRole(guestRole);
            }

            for (Role role : rolesToAdd) {
                user.addRole(role);
            }

            for (Role role : rolesToRemove) {
                user.removeRole(role);
            }

            user.setInstanceQuota(input.getInstanceQuota());
            this.userService.save(user);
            return new UserType(user);

        } catch (ParseException e) {
            throw new ValidationException(e);
        }
    }

}
