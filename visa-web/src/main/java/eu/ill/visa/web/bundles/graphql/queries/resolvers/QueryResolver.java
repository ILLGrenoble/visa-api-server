package eu.ill.visa.web.bundles.graphql.queries.resolvers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.bundles.graphql.context.AuthenticationContext;
import eu.ill.visa.web.bundles.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.bundles.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.bundles.graphql.queries.domain.InstanceStateCount;
import eu.ill.visa.web.bundles.graphql.relay.Connection;
import eu.ill.visa.web.bundles.graphql.relay.PageInfo;
import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.concurrent.CompletableFuture.runAsync;

@Singleton
public class QueryResolver implements GraphQLQueryResolver {

    private final InstrumentService            instrumentService;
    private final ExperimentService            experimentService;
    private final FlavourService               flavourService;
    private final ImageService                 imageService;
    private final InstanceService              instanceService;
    private final UserService                  userService;
    private final ImageProtocolService         imageProtocolService;
    private final RoleService                  roleService;
    private final CloudClient                  cloudClient;
    private final Mapper                       mapper;
    private final PlanService                  planService;
    private final InstanceSessionMemberService instanceSessionMemberService;
    private final InstanceJupyterSessionService instanceJupyterSessionService;
    private final SystemNotificationService systemNotificationService;


    @Inject
    QueryResolver(
        final InstrumentService instrumentService,
        final ExperimentService experimentService,
        final FlavourService flavourService,
        final ImageService imageService,
        final InstanceService instanceService,
        final UserService userService,
        final PlanService planService,
        final ImageProtocolService imageProtocolService,
        final RoleService roleService,
        final CloudClient cloudClient,
        final Mapper mapper,
        final InstanceSessionMemberService instanceSessionMemberService,
        final InstanceJupyterSessionService instanceJupyterSessionService,
        final SystemNotificationService systemNotificationService) {
        this.instrumentService = instrumentService;
        this.experimentService = experimentService;
        this.flavourService = flavourService;
        this.imageService = imageService;
        this.instanceService = instanceService;
        this.userService = userService;
        this.planService = planService;
        this.imageProtocolService = imageProtocolService;
        this.roleService = roleService;
        this.cloudClient = cloudClient;
        this.mapper = mapper;
        this.instanceSessionMemberService = instanceSessionMemberService;
        this.instanceJupyterSessionService = instanceJupyterSessionService;
        this.systemNotificationService = systemNotificationService;
    }

    /**
     * Get a list of all instruments
     *
     * @return the list of instruments ordered by name
     */
    public List<Instrument> instruments() {
        return instrumentService.getAll();
    }

    /**
     * Get a list of flavours
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of flavours
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    public Connection<Flavour> flavours(final Pagination pagination) throws DataFetchingException {
        try {
            final List<Flavour> results = flavourService.getAll(pagination);
            final PageInfo pageInfo = new PageInfo(flavourService.countAll(), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
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
    public Long countFlavours() throws DataFetchingException {
        try {
            return flavourService.countAllForAdmin();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get a list of images
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of images
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    public Connection<Image> images(final Pagination pagination) throws DataFetchingException {
        try {
            final List<Image> results = imageService.getAllForAdmin(pagination
            );
            final PageInfo pageInfo = new PageInfo(imageService.countAllForAdmin(), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Count all images
     *
     * @return a count of images
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    public Long countImages() throws DataFetchingException {
        try {
            return imageService.countAllForAdmin();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public List<ImageProtocol> imageProtocols() {
        return imageProtocolService.getAll();
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
    public Connection<Instance> instances(final QueryFilter filter, final OrderBy orderBy, final Pagination pagination) throws DataFetchingException {
        try {
            if (!pagination.isLimitBetween(0, 50)) {
                throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
            }
            final List<Instance> results = instanceService.getAll(
                requireNonNullElseGet(filter, QueryFilter::new),
                requireNonNullElseGet(orderBy, () -> new OrderBy("name", true)), pagination
            );
            final PageInfo pageInfo = new PageInfo(instanceService.countAll(filter), pagination.getLimit(), pagination.getOffset());
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
    public Long countInstances(final QueryFilter filter) throws DataFetchingException {
        try {
            return instanceService.countAll(requireNonNullElseGet(filter, QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public Instance instance(final Long id) throws EntityNotFoundException {
        final Instance instance = instanceService.getById(id);
        if (instance == null) {
            throw new EntityNotFoundException("Instance not found for the given id");
        }
        return instance;
    }

    /**
     * Get a list of recently created instances
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of recently created instances
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    public Connection<Instance> recentInstances(final Pagination pagination) throws DataFetchingException {
        return instances(new QueryFilter(), new OrderBy("createdAt", false), pagination);
    }

    public List<InstanceSession> instanceSessions() {
        return new ArrayList<>();
    }

    /**
     * Get the number of instances for a given state
     *
     * @param state the state
     * @return a count of instances
     */
    public Long countInstancesForState(InstanceState state) {
        return instanceService.countAllForState(state);
    }

    /**
     * Get the number of instances for a given list of states
     *
     * @param states the states. if no state is provided, then all states will be queried
     * @return the count of instances grouped by each state
     */
    public List<InstanceStateCount> countInstancesForStates(List<InstanceState> states) {
        final List<InstanceStateCount> results = new ArrayList<>();
        for (final InstanceState state : requireNonNullElse(states, asList(InstanceState.values()))) {
            final Long count = instanceService.countAllForState(state);
            results.add(new InstanceStateCount(state, count));
        }
        return results;
    }

    /**
     * Get a list of experiments
     *
     * @param filter     the given query filter
     * @param orderBy    the ordering of results
     * @param pagination the pagination (limit and offset)
     * @return a list of experiments
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    public Connection<Experiment> experiments(final QueryFilter filter, final OrderBy orderBy, final Pagination pagination) throws DataFetchingException {
        try {
            if (!pagination.isLimitBetween(0, 50)) {
                throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
            }
            final List<Experiment> results = experimentService.getAll(
                requireNonNullElseGet(filter, QueryFilter::new),
                requireNonNullElseGet(orderBy, () -> new OrderBy("id", true)), pagination
            );
            final PageInfo pageInfo = new PageInfo(experimentService.countAll(filter), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Count all experiments
     *
     * @param filter a filter to filter the results
     * @return a count of experiments
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    public Long countExperiments(final QueryFilter filter) throws DataFetchingException {
        try {
            return experimentService.countAll(requireNonNullElseGet(filter, QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
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
    public Connection<User> users(final QueryFilter filter, final OrderBy orderBy, Pagination pagination) throws DataFetchingException {
        try {
            if (!pagination.isLimitBetween(0, 200)) {
                throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
            }
            final List<User> results = userService.getAll(
                requireNonNullElseGet(filter, QueryFilter::new),
                requireNonNullElseGet(orderBy, () -> new OrderBy("lastName", true)), pagination
            );
            final PageInfo pageInfo = new PageInfo(userService.countAll(filter), pagination.getLimit(), pagination.getOffset());
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
    public Connection<User> recentActiveUsers(final Pagination pagination) throws DataFetchingException {
        final QueryFilter filter = new QueryFilter("lastSeenAt IS NOT NULL AND activatedAt IS NOT NULL");
        return users(filter, new OrderBy("lastSeenAt", false), pagination);
    }

    public Connection<User> searchForUserByLastName(final String lastName, final Pagination pagination) throws DataFetchingException {
        try {
            if (!pagination.isLimitBetween(0, 200)) {
                throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
            }
            final List<User> results = userService.getAllLikeLastName(lastName, pagination);
            final PageInfo pageInfo = new PageInfo(userService.countAllLikeLastName(lastName), pagination.getLimit(), pagination.getOffset());
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
    public Long countUsers(final QueryFilter filter) throws DataFetchingException {
        try {
            return userService.countAll(requireNonNullElseGet(filter, QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public Long countActivatedUsers() throws DataFetchingException {
        try {
            return userService.countAllActivated();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get a user by a given user identifier
     * This is the logged in user
     *
     * @param id the user id
     * @return the user
     * @throws EntityNotFoundException thrown if the user does not exist
     */
    public User user(final String id) throws EntityNotFoundException {
        final User user = userService.getById(id);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }
        return user;
    }

    /**
     * Get a list of all roles
     *
     * @return all the roles
     */
    public List<Role> roles() {
        return this.roleService.getAll();
    }

    /**
     * Get memory stats for the JVM
     *
     * @return the memory stats
     */
    public Memory memory() {
        final Runtime runTime = Runtime.getRuntime();
        final long total = runTime.totalMemory() / 1024 / 1024;
        final long max = runTime.maxMemory() / 1024 / 1024;
        final long free = runTime.freeMemory() / 1024 / 1024;
        return new Memory(total, max, free);
    }

    /**
     * Get the logged in user
     *
     * @param environment the graphql environment
     * @return the logged in user
     */
    public User viewer(DataFetchingEnvironment environment) {
        final AuthenticationContext context = environment.getContext();
        final AccountToken token = context.getAccountToken();
        return token.getUser();
    }

    /**
     * Get cloud images from the the cloud provider
     *
     * @return a list of cloud images
     */
    public CompletableFuture<List<CloudImage>> cloudImages() {
        final CompletableFuture<List<CloudImage>> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                future.complete(cloudClient.images());
            } catch (CloudException exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    /**
     * Get cloud flavours from the the cloud provider
     *
     * @return a list of cloud flavours
     */
    public CompletableFuture<List<CloudFlavour>> cloudFlavours() {
        final CompletableFuture<List<CloudFlavour>> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                future.complete(cloudClient.flavours());
            } catch (CloudException exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    /**
     * Get cloud limits from the the cloud provider
     *
     * @return a list of cloud limits
     */
    public CompletableFuture<CloudLimit> cloudLimits() {
        final CompletableFuture<CloudLimit> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                future.complete(cloudClient.limits());
            } catch (CloudException exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    /**
     * Get a list of plans
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of plans
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    public Connection<Plan> plans(final Pagination pagination) throws DataFetchingException {
        try {
            final List<Plan> results = planService.getAllForAdmin(pagination);
            final PageInfo pageInfo = new PageInfo(planService.countAllForAdmin(), pagination.getLimit(), pagination.getOffset());
            return new Connection<>(pageInfo, results);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
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
    public Connection<InstanceSessionMember> sessions(final QueryFilter filter, final OrderBy orderBy, Pagination pagination) throws DataFetchingException {
        try {
            final List<InstanceSessionMember> results = instanceSessionMemberService.getAll(
                requireNonNullElseGet(filter, QueryFilter::new),
                requireNonNullElseGet(orderBy, () -> new OrderBy("id", true)), pagination
            );
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
    public Long countSessions(final QueryFilter filter) throws DataFetchingException {
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
    public Long countActiveSessions(final QueryFilter filter) throws DataFetchingException {
        try {
            return instanceSessionMemberService.countAllActive(requireNonNullElseGet(filter, QueryFilter::new));
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
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
    public Connection<InstanceJupyterSession> jupyterSessions(final QueryFilter filter, final OrderBy orderBy, Pagination pagination) throws DataFetchingException {
        try {
            final List<InstanceJupyterSession> results = instanceJupyterSessionService.getAll(
                requireNonNullElseGet(filter, QueryFilter::new),
                requireNonNullElseGet(orderBy, () -> new OrderBy("id", true)), pagination
            );
            final PageInfo pageInfo = new PageInfo(instanceJupyterSessionService.countAll(filter), pagination.getLimit(), pagination.getOffset());
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
    public Long countJupyterSessions() throws DataFetchingException {
        return instanceJupyterSessionService.countAllInstances();
    }

    public List<NumberInstancesByFlavour> countInstancesByFlavours() throws DataFetchingException {
        try {
            return instanceService.countByFlavour();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public List<NumberInstancesByImage> countInstancesByImages() throws DataFetchingException {
        try {
            return instanceService.countByImage();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public List<SystemNotification> systemNotifications() throws DataFetchingException {
        try {
            return systemNotificationService.getAll();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }
}
