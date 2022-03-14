package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.SystemNotification;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class SystemNotificationRepository extends AbstractRepository<SystemNotification> {


    @Inject
    SystemNotificationRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<SystemNotification> getAll() {
        final TypedQuery<SystemNotification> query = getEntityManager().createNamedQuery("systemNotification.getAll", SystemNotification.class);
        return query.getResultList();
    }

    public List<SystemNotification> getAllActive() {
        final TypedQuery<SystemNotification> query = getEntityManager().createNamedQuery("systemNotification.getAllActive", SystemNotification.class);
        return query.getResultList();
    }

    public SystemNotification getById(final Long id) {
        try {
            final TypedQuery<SystemNotification> query = getEntityManager().createNamedQuery("systemNotification.getById", SystemNotification.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void save(final SystemNotification systemNotification) {
        if (systemNotification.getId() == null) {
            persist(systemNotification);
        } else {
            merge(systemNotification);
        }
    }

    public void delete(final SystemNotification systemNotification) {
        remove(systemNotification);
    }
}
