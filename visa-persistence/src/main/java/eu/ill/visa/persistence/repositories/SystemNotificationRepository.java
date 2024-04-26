package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.SystemNotification;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class SystemNotificationRepository extends AbstractRepository<SystemNotification> {


    @Inject
    SystemNotificationRepository(final EntityManager entityManager) {
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
