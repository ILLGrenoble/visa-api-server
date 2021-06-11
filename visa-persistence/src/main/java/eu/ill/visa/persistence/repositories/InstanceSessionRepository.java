package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSession;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class InstanceSessionRepository extends AbstractRepository<InstanceSession> {

    @Inject
    InstanceSessionRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<InstanceSession> getAll() {
        final TypedQuery<InstanceSession> query = getEntityManager().createNamedQuery("instanceSession.getAll", InstanceSession.class);
        return query.getResultList();
    }

    public InstanceSession getByInstance(final Instance instance) {
        final TypedQuery<InstanceSession> query = getEntityManager().createNamedQuery("instanceSession.getAllByInstance", InstanceSession.class);
        query.setParameter("instance", instance);
        query.setMaxResults(1);

        List<InstanceSession> sessions = query.getResultList();
        if (sessions.size() > 0) {
            return sessions.get(0);
        }
        return null;
    }


    public List<InstanceSession> getAllByInstance(final Instance instance) {
        final TypedQuery<InstanceSession> query = getEntityManager().createNamedQuery("instanceSession.getAllByInstance", InstanceSession.class);
        query.setParameter("instance", instance);

        List<InstanceSession> sessions = query.getResultList();
        return sessions;
    }


    public InstanceSession getById(final Long id) {
        try {
            final TypedQuery<InstanceSession> query = getEntityManager().createNamedQuery("instanceSession.getById", InstanceSession.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }


    public void save(final InstanceSession instanceSession) {
        if (instanceSession.getId() == null) {
            persist(instanceSession);
        } else {
            merge(instanceSession);
        }
    }
}
