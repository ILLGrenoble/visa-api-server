package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class InstanceSessionRepository extends AbstractRepository<InstanceSession> {

    @Inject
    InstanceSessionRepository(final EntityManager entityManager) {
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
        if (!sessions.isEmpty()) {
            return sessions.getFirst();
        }
        return null;
    }

    public InstanceSession getLastByInstance(final Instance instance) {
        final TypedQuery<InstanceSession> query = getEntityManager().createNamedQuery("instanceSession.getLastByInstance", InstanceSession.class);
        query.setParameter("instance", instance);
        query.setMaxResults(1);

        List<InstanceSession> sessions = query.getResultList();
        if (!sessions.isEmpty()) {
            return sessions.getFirst();
        }
        return null;
    }

    public List<InstanceSession> getAllByInstance(final Instance instance) {
        final TypedQuery<InstanceSession> query = getEntityManager().createNamedQuery("instanceSession.getAllByInstance", InstanceSession.class);
        query.setParameter("instance", instance);

        return query.getResultList();
    }

    public List<InstanceSession> getAllLatestByInstanceIdAndProtocol(Long instanceId, String protocol) {
        final TypedQuery<InstanceSession> query = getEntityManager().createNamedQuery("instanceSession.getAllByInstanceIdAndProtocol", InstanceSession.class);
        query.setParameter("instanceId", instanceId);
        query.setParameter("protocol", protocol);
        return query.getResultList();
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

    public void updatePartial(final InstanceSession instanceSession) {
        getEntityManager().createNamedQuery("instanceSession.updatePartialById")
            .setParameter("id", instanceSession.getId())
            .setParameter("current", instanceSession.getCurrent())
            .executeUpdate();
    }
}
