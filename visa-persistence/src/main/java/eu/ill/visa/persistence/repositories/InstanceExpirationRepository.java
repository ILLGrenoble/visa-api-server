package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceExpiration;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Singleton
public class InstanceExpirationRepository extends AbstractRepository<InstanceExpiration> {

    @Inject
    InstanceExpirationRepository(final Provider<EntityManager> entityManager) {
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
