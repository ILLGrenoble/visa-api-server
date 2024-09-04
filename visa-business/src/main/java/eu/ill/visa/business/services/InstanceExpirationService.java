package eu.ill.visa.business.services;

import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.business.notification.EmailManager;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.InstanceExpiration;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.persistence.repositories.InstanceExpirationRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Transactional
@Singleton
public class InstanceExpirationService {

    public static final  int                          HOURS_BEFORE_EXPIRATION_INACTIVITY = 24;
    public static final  int                          HOURS_BEFORE_EXPIRATION_LIFETIME = 48;
    private static final Logger                       logger                  = LoggerFactory.getLogger(InstanceExpirationService.class);

    private final InstanceExpirationRepository repository;
    private final InstanceService instanceService;
    private final InstanceCommandService instanceCommandService;
    private final InstanceConfiguration configuration;
    private final EmailManager emailManager;

    @Inject
    public InstanceExpirationService(InstanceExpirationRepository repository,
                                     InstanceService instanceService,
                                     InstanceCommandService instanceCommandService,
                                     InstanceConfiguration configuration,
                                     EmailManager emailManager) {
        this.repository = repository;
        this.instanceService = instanceService;
        this.instanceCommandService = instanceCommandService;
        this.configuration = configuration;
        this.emailManager = emailManager;
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

                try {
                    InstanceCommand command = instanceCommandService.create(null, instance, InstanceCommandType.DELETE);
                    this.instanceCommandService.execute(command);

                    logger.info("Deleted expired instance {}", instance.getId());

                    // Remove the InstanceExpiration
                    this.delete(instanceExpiration);

                    // Email user
                    emailManager.sendInstanceDeletedNotification(instance, instanceExpiration);

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

    public void delete(@NotNull InstanceExpiration instanceExpiration) {
        Instance instance = instanceExpiration.getInstance();
        instance.setExpirationDate(null);
        this.instanceService.save(instance);

        this.repository.delete(instanceExpiration);

    }

    public void save(@NotNull InstanceExpiration instanceExpiration) {
        Instance instance = instanceExpiration.getInstance();
        instance.setExpirationDate(instanceExpiration.getExpirationDate());
        this.instanceService.save(instance);

        this.repository.save(instanceExpiration);
    }

    public void createExpirationForAllInactiveInstances() {
        Integer userInactivityDurationHours = this.configuration.userMaxInactivityDurationHours() - HOURS_BEFORE_EXPIRATION_INACTIVITY;
        Integer staffInactivityDurationHours = this.configuration.staffMaxInactivityDurationHours() - HOURS_BEFORE_EXPIRATION_INACTIVITY;

        var userInstancesStream = this.instanceService.getAllNewInactive(userInactivityDurationHours).stream()
            .filter(instance -> !instance.getOwner().getUser().hasRole(Role.STAFF_ROLE));
        var staffInstancesStream = this.instanceService.getAllNewInactive(staffInactivityDurationHours).stream()
            .filter(instance -> instance.getOwner().getUser().hasRole(Role.STAFF_ROLE));
        var instances = Stream.concat(userInstancesStream, staffInstancesStream).toList();
        for (var instance : instances) {
            boolean isStaffOwner = instance.getOwner().getUser().hasRole(Role.STAFF_ROLE);
            var maxInactivityDurationHours = isStaffOwner
                ? this.configuration.staffMaxInactivityDurationHours()
                : this.configuration.userMaxInactivityDurationHours();
            Date expirationDate = DateUtils.addHours(instance.getLastSeenAt(), maxInactivityDurationHours);
            this.create(instance, expirationDate);
            logger.info("Scheduled expiration for instance {} due to inactivity", instance.getId());

            // Email user
            emailManager.sendInstanceExpiringNotification(instance, expirationDate);
        }
    }

    public void createExpirationForAllTerminatingInstances() {
        var instances = this.instanceService.getAllNewTerminations(HOURS_BEFORE_EXPIRATION_LIFETIME);
        for (var instance : instances) {
            if (instance.getTerminationDate() != null) {
                this.create(instance, instance.getTerminationDate());
                logger.info("Scheduled expiration for instance {} as it is reaching its lifetime limit", instance.getId());

                // Email user
                emailManager.sendInstanceLifetimeNotification(instance);
            }
        }
    }

    public void removeExpirationForAllActiveInstances() {
        var instanceExpirations = this.getAll();
        for (InstanceExpiration instanceExpiration : instanceExpirations) {
            Instance instance = instanceExpiration.getInstance();

            // check expiration due to inactivity rather than max_lifetime
            if (instance.getTerminationDate() != null && instance.getTerminationDate().compareTo(instanceExpiration.getExpirationDate()) > 0) {

                // If instance has had activity since the instance expiration was created then delete it
                if (instance.getLastSeenAt().compareTo(instanceExpiration.getCreatedAt()) > 0) {
                    logger.info("Instance expiration removed for instance {} after activity recorded", instance.getId());
                    this.delete(instanceExpiration);
                }

            } else if (instance.getTerminationDate() == null) {
                logger.info("Instance expiration removed for instance {} as it is now immortal", instance.getId());
                this.delete(instanceExpiration);
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
