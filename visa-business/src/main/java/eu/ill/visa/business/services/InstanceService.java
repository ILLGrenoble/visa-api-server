package eu.ill.visa.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.business.gateway.UserEvent;
import eu.ill.visa.business.gateway.events.InstanceStateChangedEvent;
import eu.ill.visa.business.gateway.events.InstanceThumbnailUpdatedEvent;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.persistence.repositories.InstanceRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNullElse;

@Transactional
@Singleton
public class InstanceService {
    private static final Logger logger = LoggerFactory.getLogger(InstanceService.class);

    private final InstanceRepository repository;
    private final InstanceConfiguration configuration;
    private final CloudClientService cloudClientService;
    private final InstanceMemberService instanceMemberService;
    private final EventDispatcher eventDispatcher;

    // Specifies a window in which to look for instances for which an instrument control support user can provider support.
    // Defines a window and looks for experiment schedules that overlap this window
    private static final int INSTRUMENT_CONTROL_SUPPORT_WINDOW_HALF_WIDTH_IN_DAYS = 7;

    @Inject
    public InstanceService(final InstanceRepository repository,
                           final InstanceConfiguration configuration,
                           final CloudClientService cloudClientService,
                           final InstanceMemberService instanceMemberService,
                           final EventDispatcher eventDispatcher) {
        this.repository = repository;
        this.configuration = configuration;
        this.cloudClientService = cloudClientService;
        this.instanceMemberService = instanceMemberService;
        this.eventDispatcher = eventDispatcher;
    }

    public Long countAll() {
        return this.repository.countAll();
    }

    public Long countAll(QueryFilter filter) {
        return this.repository.countAll(filter);
    }

    public Long countAllForState(InstanceState state) {
        return this.repository.countAllForState(state);
    }

    public List<Instance> getAllForUser(User user) {
        return this.repository.getAllForUser(user).stream()
            .map(Instance::lazyLoadInit)
            .toList();
    }

    public Long countAllForUser(User user) {
        return this.repository.countAllForUser(user);
    }

    public List<Instance> getAllWithStates(List<InstanceState> states) {
        return this.repository.getAllWithStates(states);
    }

    public Instance getById(Long id) {
        return this.repository.getById(id);
    }

    public Instance getFullById(Long id) {
        return this.repository.getById(id).lazyLoadInit();
    }

    public Instance getByUID(String uid) {
        return this.repository.getByUID(uid);
    }

    public Instance getFullByUID(String uid) {
        return this.repository.getByUID(uid).lazyLoadInit();
    }

    public Instance create(Instance.Builder instanceBuilder) {
        Instance instance = instanceBuilder
            .uid(this.getUID())
            .state(InstanceState.BUILDING)
            .lastSeenAt(new Date())
            .build();

        // Determine from owner whether to apply staff or user lifetime durations
        InstanceMember owner = instance.getOwner();
        if (owner != null && owner.getUser().hasRole(Role.STAFF_ROLE)) {
            long terminationDate = (new Date().getTime()) + this.configuration.staffMaxLifetimeDurationHours() * 60L * 60L * 1000L;
            instance.setTerminationDate(new Date(terminationDate));

        } else {
            long terminationDate = (new Date().getTime()) + this.configuration.userMaxLifetimeDurationHours() * 60L * 60L * 1000L;
            instance.setTerminationDate(new Date(terminationDate));
        }

        this.save(instance);

        this.sendOwnerInstanceEvent(instance, UserEvent.INSTANCES_CHANGED_EVENT);

        return instance;
    }

    public void save(Instance instance) {
        Integer oldStateHash = instance.getStateHash();
        instance.updateStateHash();
        this.repository.save(instance);

        if (!instance.getStateHash().equals(oldStateHash)) {
            if (Boolean.TRUE.equals(instance.getDeleted())) {
                this.sendOwnerInstanceEvent(instance, UserEvent.INSTANCES_CHANGED_EVENT);

            } else {
                this.sendOwnerInstanceEvent(instance, UserEvent.INSTANCE_STATE_CHANGED_EVENT, new InstanceStateChangedEvent(instance));
            }
        }
    }

    public List<Instance> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<Instance> getAllFull() {
        return this.repository.getAll().stream()
            .map(Instance::lazyLoadInit)
            .toList();
    }

    public List<Instance> getAll() {
        return this.repository.getAll();
    }

    public Map<CloudClient, List<Instance>> getAllByCloud() {
        List<Instance> instances = this.getAll();

        List<CloudClient> cloudClients = this.cloudClientService.getAll();
        Map<CloudClient, List<Instance>> cloudInstances = new HashMap<>();

        for (CloudClient cloudClient : cloudClients) {
            cloudInstances.put(cloudClient, new ArrayList<>());
        }

        instances.forEach(instance -> {
            Long cloudId = instance.getCloudId();
            CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudId);
            if (cloudClient != null) {
                cloudInstances.get(cloudClient).add(instance);

            } else {
                logger.warn("Instance with Id {} does not have a valid Cloud Provider with Id {}", instance.getId(), instance.getCloudId());
            }
        });

        return cloudInstances;
    }

    public List<Instance> getAllInactive(final Integer maxInactivityDurationHours) {
        if (maxInactivityDurationHours == null) {
            throw new IllegalArgumentException("maxInactivityDurationHours cannot be null");
        }
        return this.repository.getAllInactive(maxInactivityDurationHours);
    }

    public List<Instance> getAllNewInactive(final Integer maxInactivityDurationHours) {
        if (maxInactivityDurationHours == null) {
            throw new IllegalArgumentException("maxInactivityDurationHours cannot be null");
        }
        return this.repository.getAllNewInactive(maxInactivityDurationHours);
    }

    public List<Instance> getAllNewTerminations(final Integer terminationInHours) {
        if (terminationInHours == null) {
            throw new IllegalArgumentException("terminationInHours cannot be null");
        }
        return this.repository.getAllNewTerminations(terminationInHours);
    }

    public List<Instance> getAllToDelete() {
        return this.repository.getAllToDelete().stream()
            .map(Instance::lazyLoadInit)
            .toList();
    }

    public Long countAllForInstrumentScientist(User user, InstanceFilter filter) {
        return this.repository.countAllForInstrumentScientist(user, filter);
    }

    public List<Instance> getAllForInstrumentScientist(User user, InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAllForInstrumentScientist(user, filter, orderBy, pagination);
    }

    public Long countAllForInstrumentControlSupport(InstanceFilter filter) {
        return this.repository.countAllForInstrumentControlSupport(INSTRUMENT_CONTROL_SUPPORT_WINDOW_HALF_WIDTH_IN_DAYS, filter);
    }

    public List<Instance> getAllForInstrumentControlSupport(InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAllForInstrumentControlSupport(INSTRUMENT_CONTROL_SUPPORT_WINDOW_HALF_WIDTH_IN_DAYS, filter, orderBy, pagination);
    }

    public Long countAllForITSupport(InstanceFilter filter) {
        return this.repository.countAllForITSupport(filter);
    }

    public List<Instance> getAllForITSupport(InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAllForITSupport(filter, orderBy, pagination);
    }

    public Instance getByIdForInstrumentScientist(User user, Long instanceId) {
        return this.repository.getByIdForInstrumentScientist(user, instanceId);
    }

    public Instance getByIdForInstrumentControlSupport(Long id) {
        return this.repository.getByIdForInstrumentControlSupport(id, INSTRUMENT_CONTROL_SUPPORT_WINDOW_HALF_WIDTH_IN_DAYS);
    }

    public Instance getByIdForITSupport(Long id) {
        return this.repository.getByIdForITSupport(id);
    }

    public Instance getDeletedInstanceByComputeId(String computeId) {
        return this.repository.getDeletedInstanceByComputeId(computeId);
    }

    public Long countAllForSupportUser(User user, InstanceFilter filter) {
        if (user.hasRole(Role.INSTRUMENT_CONTROL_ROLE)) {
            return this.countAllForInstrumentControlSupport(filter);

        } else if (user.hasRole(Role.IT_SUPPORT_ROLE)) {
            return this.countAllForITSupport(filter);

        } else if (user.hasRole(Role.INSTRUMENT_SCIENTIST_ROLE)) {
            return this.countAllForInstrumentScientist(user, filter);
        }

        return 0L;
    }

    public boolean isOwnerOrAdmin(User user, Instance instance) {
        return instance.isOwner(user) || user.hasRole(Role.ADMIN_ROLE);
    }

    public boolean isAuthorisedForInstance(User user, Instance instance) {
        if (instance.isMember(user)) {
            return true;
        }

        return this.isInstanceSupport(user, instance);
    }


    public boolean isInstanceSupport(User user, Instance instance) {
        // Check specific instances for the different support roles
        if (user.hasRole(Role.ADMIN_ROLE)) {
            return true;

        } else if (user.hasRole(Role.IT_SUPPORT_ROLE)) {
            Instance instanceForITSupport = this.getByIdForITSupport(instance.getId());
            return (instanceForITSupport != null);

        } else if (user.hasRole(Role.INSTRUMENT_CONTROL_ROLE)) {
            Instance instanceForInstrumentControl = this.getByIdForInstrumentControlSupport(instance.getId());
            return (instanceForInstrumentControl != null);

        } else if (user.hasRole(Role.INSTRUMENT_SCIENTIST_ROLE)) {
            Instance instanceForInstrumentScientist = this.getByIdForInstrumentScientist(user, instance.getId());
            return (instanceForInstrumentScientist != null);
        }

        return false;
    }

    public List<NumberInstancesByFlavour> countByFlavour() {
        return this.repository.countByFlavour();
    }

    public List<NumberInstancesByImage> countByImage() {
        return this.repository.countByImage();
    }

    public List<NumberInstancesByCloudClient> countByCloudClient() {
        return this.repository.countByCloudClient();
    }

    private void sendOwnerInstanceEvent(final Instance instance, final String eventType) {
        this.sendOwnerInstanceEvent(instance, eventType, null);
    }

    private void sendOwnerInstanceEvent(final Instance instance, final String eventType, final Object event) {
        final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
        if (owner != null) {
            this.eventDispatcher.sendEventToUser(owner.getId(), eventType, event);
        }
    }

    private List<Instance> getAllForSupportByUserRole(User user, InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        if (user.hasRole(Role.INSTRUMENT_CONTROL_ROLE)) {
            return this.getAllForInstrumentControlSupport(filter, orderBy, pagination);

        } else if (user.hasRole(Role.IT_SUPPORT_ROLE)) {
            return this.getAllForITSupport(filter, orderBy, pagination);

        } else if (user.hasRole(Role.INSTRUMENT_SCIENTIST_ROLE)) {
            return this.getAllForInstrumentScientist(user, filter, orderBy, pagination);
        }

        return new ArrayList<>();
    }

    public List<Instance> getAllForSupportUser(User user, InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.getAllForSupportByUserRole(user, filter, orderBy, pagination).stream()
            .map(Instance::lazyLoadInit)
            .toList();
    }

    public Long countAllForUserAndRole(User user, InstanceMemberRole role) {
        return repository.countAllForUserAndRole(user, role);
    }

    public InstanceThumbnail createOrUpdateThumbnail(Instance instance, byte[] data) {
        final InstanceThumbnail thumbnail = requireNonNullElse(repository.getThumbnailForInstance(instance), new InstanceThumbnail());
        if (thumbnail.getInstance() == null) {
            thumbnail.setInstance(instance);
        }
        thumbnail.setData(data);
        this.repository.saveThumbnail(thumbnail);

        this.sendOwnerInstanceEvent(instance, UserEvent.INSTANCE_THUMBNAIL_UPDATED_EVENT, new InstanceThumbnailUpdatedEvent(instance));

        return thumbnail;
    }

    public InstanceThumbnail getThumbnailForInstance(Instance instance) {
        return repository.getThumbnailForInstance(instance);
    }

    public String getUID() {
        String regex = "^.*[a-zA-Z]+.*";
        Pattern pattern = Pattern.compile(regex);

        do {
            String uid = RandomStringUtils.randomAlphanumeric(8);

            // Ensure UID has at least one character to make it distinguishable from a valid ID
            Matcher matcher = pattern.matcher(uid);

            if (matcher.matches() && this.repository.getByUID(uid) == null) {
                return uid;
            }
        } while (true);
    }
}
