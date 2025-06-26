package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.AcknowledgedSystemNotification;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class AcknowledgedSystemNotificationRepository extends AbstractRepository<AcknowledgedSystemNotification> {

    @Inject
    AcknowledgedSystemNotificationRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<AcknowledgedSystemNotification> getAllByUserId(final String userId) {
        final TypedQuery<AcknowledgedSystemNotification> query = getEntityManager().createNamedQuery("acknowledgedSystemNotification.getAllByUserId", AcknowledgedSystemNotification.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public void save(AcknowledgedSystemNotification acknowledgedNotification) {
        if (acknowledgedNotification.getId() == null) {
            persist(acknowledgedNotification);

        } else {
            merge(acknowledgedNotification);
        }
    }
}
