package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.AcknowledgedSystemNotification;
import eu.ill.visa.core.entity.SystemNotification;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.AcknowledgedSystemNotificationRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@Singleton
public class AcknowledgedSystemNotificationService {

    private final AcknowledgedSystemNotificationRepository repository;
    @Inject
    AcknowledgedSystemNotificationService(final AcknowledgedSystemNotificationRepository repository) {
        this.repository = repository;
    }

    public List<AcknowledgedSystemNotification> getAllByUserId(final String userId) {
        return this.repository.getAllByUserId(userId);
    }

    public void acknowledgeSystemNotification(SystemNotification notification, User user) {
        AcknowledgedSystemNotification acknowledgedNotification = new AcknowledgedSystemNotification(notification, user);
        this.repository.save(acknowledgedNotification);
    }
}
