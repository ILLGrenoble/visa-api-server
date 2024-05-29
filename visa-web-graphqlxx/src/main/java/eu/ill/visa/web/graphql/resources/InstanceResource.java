package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.inputs.OrderByInput;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.inputs.QueryFilterInput;
import eu.ill.visa.web.graphql.types.Connection;
import eu.ill.visa.web.graphql.types.PageInfo;
import eu.ill.visa.web.graphql.types.*;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.ArrayList;
import java.util.List;

import static eu.ill.visa.web.graphql.inputs.OrderByInput.toOrderBy;
import static eu.ill.visa.web.graphql.inputs.PaginationInput.toPagination;
import static eu.ill.visa.web.graphql.inputs.QueryFilterInput.toQueryFilter;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class InstanceResource {

    private final InstanceService instanceService;
    private final CloudClientGateway cloudClientGateway;

    @Inject
    public InstanceResource(final InstanceService instanceService,
                            final CloudClientGateway cloudClientGateway) {
        this.instanceService = instanceService;
        this.cloudClientGateway = cloudClientGateway;
    }


    /**
     * Get a list of instances
     *
     * @param filter     the given query filter
     * @param orderBy    the ordering of results
     * @param pagination the pagination (limit and offset)
     * @return a list of instances
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull Connection<InstanceType> instances(final QueryFilterInput filter, final OrderByInput orderBy, @NotNull final PaginationInput pagination) throws DataFetchingException {
        try {
            if (!pagination.isLimitBetween(0, 50)) {
                throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
            }
            final List<InstanceType> results = instanceService.getAll(
                requireNonNullElseGet(toQueryFilter(filter), QueryFilter::new),
                requireNonNullElseGet(toOrderBy(orderBy), () -> new OrderBy("name", true)), toPagination(pagination)
            ).stream()
                .map(InstanceType::new)
                .toList();
            final PageInfo pageInfo = new PageInfo(instanceService.countAll(toQueryFilter(filter)), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Count all instances
     *
     * @param filter a filter to filter the results
     * @return a count of instances
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countInstances(final QueryFilterInput filter) throws DataFetchingException {
        try {
            return instanceService.countAll(requireNonNullElseGet(toQueryFilter(filter), QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    @Query
    public InstanceType instance(final @NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final Instance instance = instanceService.getById(id);
        if (instance == null) {
            throw new EntityNotFoundException("Instance not found for the given id");
        }
        return new InstanceType(instance);
    }

    /**
     * Get a list of recently created instances
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of recently created instances
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull Connection<InstanceType> recentInstances(final @NotNull PaginationInput pagination) throws DataFetchingException {
        return instances(new QueryFilterInput(), new OrderByInput("createdAt", false), pagination);
    }

    /**
     * Get the number of instances for a given state
     *
     * @param state the state
     * @return a count of instances
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countInstancesForState(@NotNull InstanceState state) {
        return instanceService.countAllForState(state);
    }

    /**
     * Get the number of instances for a given list of states
     *
     * @param states the states. if no state is provided, then all states will be queried
     * @return the count of instances grouped by each state
     */
    @Query
    public @NotNull List<InstanceStateCount> countInstancesForStates(List<InstanceState> states) {
        final List<InstanceStateCount> results = new ArrayList<>();
        for (final InstanceState state : requireNonNullElse(states, asList(InstanceState.values()))) {
            final Long count = instanceService.countAllForState(state);
            results.add(new InstanceStateCount(state, count));
        }
        return results;
    }

    @Query
    public @NotNull List<NumberInstancesByFlavourType> countInstancesByFlavours() throws DataFetchingException {
        try {
            return instanceService.countByFlavour().stream()
                .map(NumberInstancesByFlavourType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    @Query
    public @NotNull List<NumberInstancesByImageType> countInstancesByImages() throws DataFetchingException {
        try {
            return instanceService.countByImage().stream()
                .map(NumberInstancesByImageType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    @Query
    public @NotNull List<NumberInstancesByCloudClientType> countInstancesByCloudClients() throws DataFetchingException {
        try {
            return instanceService.countByCloudClient().stream()
                .map(countByCloudClient -> {
                    CloudClient cloudClient = this.cloudClientGateway.getCloudClient(countByCloudClient.getId());
                    return new NumberInstancesByCloudClient(cloudClient.getId(), cloudClient.getName(), countByCloudClient.getTotal());
                })
                .map(NumberInstancesByCloudClientType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
