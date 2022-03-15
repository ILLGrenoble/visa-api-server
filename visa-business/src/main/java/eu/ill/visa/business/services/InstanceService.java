package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceMemberRole;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import eu.ill.visa.persistence.repositories.InstanceRepository;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNullElse;

@Transactional
@Singleton
public class InstanceService {

    private final InstanceRepository repository;
    private InstanceConfiguration configuration;

    // Specifies a window in which to look for instances for which an instrument control support user can provider support.
    // Defines a window and looks for experiment schedules that overlap this window
    private static final int INSTRUMENT_CONTROL_SUPPORT_WINDOW_HALF_WIDTH_IN_DAYS = 7;

    @Inject
    public InstanceService(InstanceRepository repository, InstanceConfiguration configuration) {
        this.repository = repository;
        this.configuration = configuration;
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
        return this.repository.getAllForUser(user);
    }

    public Long countAllForUser(User user) {
        return this.repository.countAllForUser(user);
    }

    public List<Instance> getAllWithStates(List<InstanceState> states) {
        return this.repository.getAllWithStates(states);
    }

    public Instance getInstanceForMember(InstanceMember member) {
        return this.repository.getInstanceForMember(member);
    }

    public Instance getById(Long id) {
        return this.repository.getById(id);
    }

    public Instance getByUID(String uid) {
        return this.repository.getByUID(uid);
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
            long terminationDate = (new Date().getTime()) + this.configuration.getStaffMaxLifetimeDurationHours() * 60L * 60L * 1000L;
            instance.setTerminationDate(new Date(terminationDate));

        } else {
            long terminationDate = (new Date().getTime()) + this.configuration.getUserMaxLifetimeDurationHours() * 60L * 60L * 1000L;
            instance.setTerminationDate(new Date(terminationDate));
        }

        this.save(instance);

        return instance;
    }

    public void save(Instance instance) {
        this.repository.save(instance);
    }

    public List<Instance> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<Instance> getAll() {
        return this.repository.getAll();
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
        return this.repository.getAllToDelete();
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


    public boolean isAuthorisedForInstance(User user, Instance instance) {
        return this.isAuthorisedForInstance(user, instance, null);
    }

    public boolean isAuthorisedForInstance(User user, Instance instance, InstanceMemberRole role) {
        final InstanceMember member = instance.getMember(user);

        if (member == null) {
            if (!user.hasAnyRole(List.of(Role.ADMIN_ROLE, Role.IT_SUPPORT_ROLE, Role.INSTRUMENT_CONTROL_ROLE, Role.INSTRUMENT_SCIENTIST_ROLE))) {
                return false;
            }

            if (!user.hasRole(Role.ADMIN_ROLE)) {
                // Check specific instances for the different support roles
                if (user.hasRole(Role.IT_SUPPORT_ROLE)) {
                    Instance instanceForITSupport = this.getByIdForITSupport(instance.getId());
                    return (instanceForITSupport != null);

                } else if (user.hasRole(Role.INSTRUMENT_CONTROL_ROLE)) {
                    Instance instanceForInstrumentControl = this.getByIdForInstrumentControlSupport(instance.getId());
                    return (instanceForInstrumentControl != null);

                } else if (user.hasRole(Role.INSTRUMENT_SCIENTIST_ROLE)) {
                    Instance instanceForInstrumentScientist = this.getByIdForInstrumentScientist(user, instance.getId());
                    return (instanceForInstrumentScientist != null);
                }
            }

        } else if (role != null && !member.isRole(role)) {
            return false;
        }

        return true;
    }


    public List<NumberInstancesByFlavour> countByFlavour() {
        return this.repository.countByFlavour();
    }

    public List<NumberInstancesByImage> countByImage() {
        return this.repository.countByImage();
    }

    public List<Instance> getAllForSupportUser(User user, InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        if (user.hasRole(Role.INSTRUMENT_CONTROL_ROLE)) {
            return this.getAllForInstrumentControlSupport(filter, orderBy, pagination);

        } else if (user.hasRole(Role.IT_SUPPORT_ROLE)) {
            return this.getAllForITSupport(filter, orderBy, pagination);

        } else if (user.hasRole(Role.INSTRUMENT_SCIENTIST_ROLE)) {
            return this.getAllForInstrumentScientist(user, filter, orderBy, pagination);
        }

        return new ArrayList<>();
    }

    public List<Instance> getAllForUserAndRole(User user, InstanceMemberRole role) {
        return repository.getAllForUserAndRole(user, role);
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
