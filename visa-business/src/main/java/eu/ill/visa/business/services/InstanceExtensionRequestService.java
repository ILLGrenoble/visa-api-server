package eu.ill.visa.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.business.gateway.AdminEvent;
import eu.ill.visa.business.notification.EmailManager;
import eu.ill.visa.core.domain.SimpleDuration;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceExtensionRequestState;
import eu.ill.visa.persistence.repositories.InstanceExtensionRequestRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Date;
import java.util.List;

@Transactional
@Singleton
public class InstanceExtensionRequestService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceExtensionRequestService.class);

    private final InstanceExtensionRequestRepository repository;
    private final InstanceService instanceService;
    private final InstanceExpirationService instanceExpirationService;
    private final EmailManager emailManager;
    private final EventDispatcher eventDispatcher;

    @Inject
    public InstanceExtensionRequestService(final InstanceExtensionRequestRepository repository,
                                           final InstanceService instanceService,
                                           final InstanceExpirationService instanceExpirationService,
                                           final EmailManager emailManager, EventDispatcher eventDispatcher) {
        this.repository = repository;
        this.instanceService = instanceService;
        this.instanceExpirationService = instanceExpirationService;
        this.emailManager = emailManager;
        this.eventDispatcher = eventDispatcher;
    }

    public InstanceExtensionRequest getById(Long id) {
        return this.repository.getById(id);
    }

    public List<InstanceExtensionRequest> getAll() {
        return this.repository.getAll();
    }

    public InstanceExtensionRequest getForInstance(Instance instance) {
        return this.repository.getForInstance(instance);
    }

    public void save(@NotNull InstanceExtensionRequest instanceMember) {
        // Event notification to admin
        this.eventDispatcher.sendEventForRole(Role.ADMIN_ROLE, AdminEvent.EXTENSION_REQUESTS_CHANGED);

        this.repository.save(instanceMember);
    }

    public InstanceExtensionRequest create(Instance instance, String comments) {
        InstanceExtensionRequest request = new InstanceExtensionRequest(instance, comments);
        this.save(request);

        final Image.AutoAcceptExtensionRequest autoAcceptExtensionRequest = instance.getPlan().getImage().getAutoAcceptExtensionRequest();
        final boolean autoAccept = autoAcceptExtensionRequest != null &&
            (autoAcceptExtensionRequest.equals(Image.AutoAcceptExtensionRequest.ALL) ||
                autoAcceptExtensionRequest.equals(Image.AutoAcceptExtensionRequest.STAFF) && instance.getOwner().getUser().hasRole(Role.STAFF_ROLE));

        if (autoAccept) {
            // Automatically grant the extension if the auto-accept policy is set to ALL
            final User user = instance.getOwner().getUser();
            final Flavour flavour = instance.getPlan().getFlavour();
            final Duration maxInstanceDuration = this.instanceService.getMaxInstanceDuration(instance.getOwner(), flavour);
            Long instanceDuration = instance.getLifetimeMinutes();
            if (instanceDuration == null || instanceDuration == 0 || instanceDuration > maxInstanceDuration.toMinutes()) {
                instanceDuration = maxInstanceDuration.toMinutes();
            }

            logger.info("Automatically granting extension for instance {} (owner {}, flavour {}) due to auto-accept policy. Extension duration calculated as {}", instance.getId(), user.getFullName(), flavour.getName(), new SimpleDuration(instanceDuration).getDurationText());

            instance.setLifetimeMinutes(instanceDuration);
            final Date terminationDate = this.instanceService.calculateTerminationDate(instance.getTerminationDate(), Duration.ofMinutes(instanceDuration));

            request.setState(InstanceExtensionRequestState.ACCEPTED);
            request.setHandledOn(new Date());
            request.setExtensionDate(terminationDate);
            this.save(request);

            this.grantExtension(instance, terminationDate, null, true);

            // Send email to admin
            comments = comments == null ? "" : comments + "\n\n--------------------\n\n";
            comments += "Extension request has been automatically accepted!";
            this.emailManager.sendInstanceExtensionRequestNotification(instance, comments, true);

        } else {
            logger.info("Instance extension request created for instance {} with comments: {}. Email sent to admins.", instance.getId(), comments);

            // Send email to admin
            this.emailManager.sendInstanceExtensionRequestNotification(instance, comments, false);

        }

        return request;
    }

    public void grantExtension(Instance instance, Date terminationDate, String handlerComments, boolean sendNotification) {
        instance.setTerminationDate(terminationDate);

        // Update the instance
        this.instanceService.save(instance);

        // Delete any existing expirations
        InstanceExpiration expiration = this.instanceExpirationService.getByInstance(instance);
        if (expiration != null) {
            this.instanceExpirationService.delete(expiration);
        }

        // Email owner
        if (sendNotification) {
            this.emailManager.sendInstanceExtensionNotification(instance, true, handlerComments);
        }
    }

    public void refuseExtension(Instance instance, String handlerComments) {
        // Email owner
        this.emailManager.sendInstanceExtensionNotification(instance, false, handlerComments);
    }
}
