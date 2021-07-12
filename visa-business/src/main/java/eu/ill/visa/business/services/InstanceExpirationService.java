package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.InstanceExpiration;
import eu.ill.visa.core.domain.Role;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import eu.ill.visa.persistence.repositories.InstanceExpirationRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
@Singleton
public class InstanceExpirationService {

    public static final  int                          HOURS_BEFORE_EXPIRATION = 24;
    private static final Logger                       logger                  = LoggerFactory.getLogger(InstanceExpirationService.class);
    private              InstanceExpirationRepository repository;
    private              InstanceService              instanceService;
    private              InstanceCommandService       instanceCommandService;
    private              InstanceConfiguration        configuration;
    private              NotificationService          notificationService;

    @Inject
    public InstanceExpirationService(InstanceExpirationRepository repository,
                                     InstanceService instanceService,
                                     InstanceCommandService instanceCommandService,
                                     InstanceConfiguration configuration,
                                     NotificationService notificationService) {
        this.repository = repository;
        this.instanceService = instanceService;
        this.instanceCommandService = instanceCommandService;
        this.configuration = configuration;
        this.notificationService = notificationService;
    }

    public List<InstanceExpiration> getAll() {
        return this.repository.getAll();
    }

    public InstanceExpiration getById(@NotNull Long id) {
        return this.repository.getById(id);
    }

    public InstanceExpiration getByInstance(@NotNull Instance instance) {
        // Will return those that have deletion pending (due to inactivity or from instance lifetime limit)
        return this.repository.getByInstance(instance);
    }

    public Date getExpirationDate(@NotNull Instance instance) {
        // Get from inactivity
        InstanceExpiration instanceExpiration = this.getByInstance(instance);
        if (instanceExpiration != null) {
            return this.repository.getByInstance(instance).getExpirationDate();
        }

        // Return max lifetime
        return instance.getTerminationDate();
    }

    public List<InstanceExpiration> getAllExpired() {
        return this.repository.getAllExpired(new Date());
    }

    public void deleteAllExpired() {
        // Find all instances that have expired
        var instanceExpirations = this.getAllExpired();
        for (InstanceExpiration instanceExpiration : instanceExpirations) {
            Instance instance = instanceExpiration.getInstance();

            // Create action to delete the instance (if not already deleted)
            if (!instance.getDeleted()) {
                logger.info("Deleting expired instance {}", instance.getId());
                InstanceState previousState = instance.getState();

                instance.setState(InstanceState.DELETING);
                this.instanceService.save(instance);

                InstanceCommand command = instanceCommandService.create(null, instance, InstanceCommandType.DELETE);
                try {
                    // Execute deletion - futureInstance should be null since no longer exists in DB
                    Instance futureInstance = this.instanceCommandService.execute(command).getFutureInstance();
                    if (futureInstance == null) {
                        logger.info("Deleted expired instance {}", instance.getId());

                        // Remove the InstanceExpiration
                        this.delete(instanceExpiration);

                        // Email user
                        notificationService.sendInstanceDeletedNotification(instance, instanceExpiration);

                    } else {
                        logger.error("Failed to delete expired instance {}, current state is {}", instance.getId(), instance.getState().toString());
                    }

                } catch (Exception e) {
                    logger.error("Caught an exception while deleting instance {}: {}", instance.getId(), e.getMessage());
                    instance.setState(previousState);
                    this.instanceService.save(instance);
                }

            } else {
                // If instance deleted then delete the expiration too
                this.delete(instanceExpiration);
            }
        }
    }

    public InstanceExpiration create(@NotNull Instance instance, @NotNull Date expirationDate) {
        // See if an expiration already exists for the instance
        InstanceExpiration existingExpiration = this.getByInstance(instance);
        if (existingExpiration != null) {
            logger.info("An instance expiration was requested for an instance {} that is already set to be expired", instance.getId());
            return existingExpiration;

        } else {
            InstanceExpiration expiration = new InstanceExpiration(instance, expirationDate);

            this.save(expiration);

            return expiration;
        }

    }

    public void delete(@NotNull InstanceExpiration InstanceExpiration) {
        this.repository.delete(InstanceExpiration);
    }

    public void save(@NotNull InstanceExpiration instanceExpiration) {
        this.repository.save(instanceExpiration);
    }

    public void createExpirationForAllInactiveInstances() {
        Integer userInactivityDurationHours = this.configuration.getUserMaxInactivityDurationHours() - HOURS_BEFORE_EXPIRATION;
        Integer staffInactivityDurationHours = this.configuration.getStaffMaxInactivityDurationHours() - HOURS_BEFORE_EXPIRATION;

        var userInstancesStream = this.instanceService.getAllNewInactive(userInactivityDurationHours).stream()
            .filter(instance -> !instance.getOwner().getUser().hasRole(Role.STAFF_ROLE));
        var staffInstancesStream = this.instanceService.getAllNewInactive(staffInactivityDurationHours).stream()
            .filter(instance -> instance.getOwner().getUser().hasRole(Role.STAFF_ROLE));
        var instances = Stream.concat(userInstancesStream, staffInstancesStream).collect(Collectors.toUnmodifiableList());
        for (var instance : instances) {
            boolean isStaffOwner = instance.getOwner().getUser().hasRole(Role.STAFF_ROLE);
            var maxInactivityDurationHours = isStaffOwner
                ? this.configuration.getStaffMaxInactivityDurationHours()
                : this.configuration.getUserMaxInactivityDurationHours();
            Date expirationDate = DateUtils.addHours(instance.getLastSeenAt(), maxInactivityDurationHours);
            this.create(instance, expirationDate);
            logger.info("Scheduled expiration for instance {} due to inactivity", instance.getId());

            // Email user
            notificationService.sendInstanceExpiringNotification(instance, expirationDate);
        }
    }

    public void createExpirationForAllTerminatingInstances() {
        var instances = this.instanceService.getAllNewTerminations(HOURS_BEFORE_EXPIRATION);
        for (var instance : instances) {
            if (instance.getTerminationDate() != null) {
                this.create(instance, instance.getTerminationDate());
                logger.info("Scheduled expiration for instance {} as it is reaching its lifetime limit", instance.getId());

                // Email user
                notificationService.sendInstanceLifetimeNotification(instance);
            }
        }
    }

    public void removeExpirationForAllActiveInstances() {
        var instanceExpirations = this.getAll();
        for (InstanceExpiration instanceExpiration : instanceExpirations) {
            Instance instance = instanceExpiration.getInstance();

            // check expiration due to inactivity rather than max_lifetime
            if (instance.getTerminationDate() == null || instance.getTerminationDate().compareTo(instanceExpiration.getExpirationDate()) > 0) {

                // If instance has had activity since the instance expiration was created then delete it
                if (instance.getLastSeenAt().compareTo(instanceExpiration.getCreatedAt()) > 0) {
                    logger.info("Instance expiration removed for instance {} after activity recorded", instance.getId());
                    this.delete(instanceExpiration);
                }
            }
        }
    }

    public void onInstanceActivated(Instance instance) {
        // See if an expiration exists for the instance
        InstanceExpiration instanceExpiration = this.getByInstance(instance);
        if (instanceExpiration != null) {

            // check expiration due to inactivity rather than max_lifetime
            if (instance.getTerminationDate() == null || instance.getTerminationDate().compareTo(instanceExpiration.getExpirationDate()) > 0) {
                logger.info("Instance expiration removed for instance {} after being activated", instance.getId());
                this.delete(instanceExpiration);
            }
        }
    }
}
