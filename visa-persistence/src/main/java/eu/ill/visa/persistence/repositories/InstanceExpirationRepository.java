package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceExpiration;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class InstanceExpirationRepository extends AbstractRepository<InstanceExpiration> {

    @Inject
    InstanceExpirationRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceExpiration> getAll() {
        final TypedQuery<InstanceExpiration> query = getEntityManager()
            .createNamedQuery("instanceExpiration.getAll", InstanceExpiration.class);
        return query.getResultList();
    }

    public InstanceExpiration getById(final Long id) {
        try {
            final TypedQuery<InstanceExpiration> query = getEntityManager()
                .createNamedQuery("instanceExpiration.getById", InstanceExpiration.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public InstanceExpiration getByInstance(final Instance instance) {
        try {
            final TypedQuery<InstanceExpiration> query = getEntityManager()
                .createNamedQuery("instanceExpiration.getByInstance", InstanceExpiration.class);
            query.setParameter("instance", instance);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<InstanceExpiration> getAllExpired(Date expirationDate) {
        final TypedQuery<InstanceExpiration> query = getEntityManager()
            .createNamedQuery("instanceExpiration.getAllExpired", InstanceExpiration.class);
        query.setParameter("expirationDate", expirationDate);
        return query.getResultList();
    }

    public void delete(final InstanceExpiration instanceExpiration) {
        remove(instanceExpiration);
    }

    public void save(final InstanceExpiration instanceExpiration) {
        if (instanceExpiration.getId() == null) {
            persist(instanceExpiration);
        } else {
            merge(instanceExpiration);
        }
    }
}
