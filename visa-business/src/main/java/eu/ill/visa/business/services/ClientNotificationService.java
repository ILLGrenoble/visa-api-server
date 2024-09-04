package eu.ill.visa.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.business.gateway.GlobalEvent;
import eu.ill.visa.core.domain.ClientNotification;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceExtensionRequest;
import eu.ill.visa.core.entity.SystemNotification;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.persistence.repositories.SystemNotificationRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
@Singleton
public class ClientNotificationService {

    private final SystemNotificationRepository systemNotificationRepository;
    private final InstanceService instanceService;
    private final InstanceExtensionRequestService instanceExtensionRequestService;

    private final static String INSTANCES_IN_ERROR = "instance.errors";
    private final static String INSTANCE_EXTENSION_REQUESTS = "instance.extension.requests";
    private final EventDispatcher eventDispatcher;

    @Inject
    public ClientNotificationService(final SystemNotificationRepository systemNotificationRepository,
                                     final InstanceService instanceService,
                                     final InstanceExtensionRequestService instanceExtensionRequestService,
                                     final EventDispatcher eventDispatcher) {
        this.systemNotificationRepository = systemNotificationRepository;
        this.instanceService = instanceService;
        this.instanceExtensionRequestService = instanceExtensionRequestService;
        this.eventDispatcher = eventDispatcher;
    }

    public List<SystemNotification> getAllSystemNotifications() {
        return this.systemNotificationRepository.getAll();
    }

    public List<SystemNotification> getAllActiveSystemNotifications() {
        return this.systemNotificationRepository.getAllActive();
    }

    public SystemNotification getSystemNotificationById(Long id) {
        return this.systemNotificationRepository.getById(id);
    }

    public void saveSystemNotification(@NotNull SystemNotification systemNotification) {
        this.systemNotificationRepository.save(systemNotification);

        this.eventDispatcher.broadcastEvent(GlobalEvent.NOTIFICATIONS_CHANGED_EVENT);
    }

    public void deleteSystemNotification(SystemNotification systemNotification) {
        systemNotification.setDeletedAt(new Date());
        this.saveSystemNotification(systemNotification);

        this.eventDispatcher.broadcastEvent(GlobalEvent.NOTIFICATIONS_CHANGED_EVENT);
    }

    public List<ClientNotification> getAllAdminNotifications() {
        List<ClientNotification> clientNotifications = new ArrayList<>();

        // Instances in error
        List<Instance> instances = this.instanceService.getAllWithStates(List.of(InstanceState.ERROR));
        if (!instances.isEmpty()) {
            clientNotifications.add(new ClientNotification(INSTANCES_IN_ERROR, instances.size()));
        }

        // Instance extension requests
        List<InstanceExtensionRequest> instanceExtensionRequests = this.instanceExtensionRequestService.getAll();
        if (!instanceExtensionRequests.isEmpty()) {
            clientNotifications.add(new ClientNotification(INSTANCE_EXTENSION_REQUESTS, instanceExtensionRequests.size()));
        }

        return clientNotifications;
    }
}
