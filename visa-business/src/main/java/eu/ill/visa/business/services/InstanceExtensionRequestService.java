package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceExpiration;
import eu.ill.visa.core.domain.InstanceExtensionRequest;
import eu.ill.visa.persistence.repositories.InstanceExtensionRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Transactional
@Singleton
public class InstanceExtensionRequestService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceExtensionRequestService.class);

    private final InstanceExtensionRequestRepository repository;
    private final InstanceService instanceService;
    private final InstanceExpirationService instanceExpirationService;
    private final NotificationService notificationService;

    @Inject
    public InstanceExtensionRequestService(final InstanceExtensionRequestRepository repository,
                                           final InstanceService instanceService,
                                           final InstanceExpirationService instanceExpirationService,
                                           final NotificationService notificationService) {
        this.repository = repository;
        this.instanceService = instanceService;
        this.instanceExpirationService = instanceExpirationService;
        this.notificationService = notificationService;
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
        this.repository.save(instanceMember);
    }

    public InstanceExtensionRequest create(Instance instance, String comments) {
        InstanceExtensionRequest request = new InstanceExtensionRequest(instance, comments);
        this.save(request);

        // Send email to admin
        this.notificationService.sendInstanceExtensionRequestNotification(instance, comments);

        return request;
    }

    public void grantExtension(Instance instance, Date terminationDate, String handlerComments) {
        instance.setTerminationDate(terminationDate);

        // Update the instance
        this.instanceService.save(instance);

        // Delete any existing expirations
        InstanceExpiration expiration = this.instanceExpirationService.getByInstance(instance);
        if (expiration != null) {
            this.instanceExpirationService.delete(expiration);
        }

        // Email owner
        this.notificationService.sendInstanceExtensionNotification(instance, true, handlerComments);
    }

    public void refuseExtension(Instance instance, String handlerComments) {
        // Email owner
        this.notificationService.sendInstanceExtensionNotification(instance, false, handlerComments);
    }
}
