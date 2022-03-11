package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.SystemNotification;
import eu.ill.visa.persistence.repositories.SystemNotificationRepository;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Transactional
@Singleton
public class SystemNotificationService {

    private SystemNotificationRepository repository;

    @Inject
    public SystemNotificationService(SystemNotificationRepository repository) {
        this.repository = repository;
    }

    public List<SystemNotification> getAll() {
        return this.repository.getAll();
    }

    public List<SystemNotification> getAllActive() {
        return this.repository.getAllActive();
    }

    public SystemNotification getById(Long id) {
        return this.repository.getById(id);
    }

    public void save(@NotNull SystemNotification systemNotification) {
        this.repository.save(systemNotification);
    }

    public void delete(SystemNotification systemNotification) {
        systemNotification.setDeletedAt(new Date());
        this.save(systemNotification);
    }
}
