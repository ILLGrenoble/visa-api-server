package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceJupyterSession;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class InstanceJupyterSessionRepository extends AbstractRepository<InstanceJupyterSession> {

    @Inject
    InstanceJupyterSessionRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceJupyterSession> getAll() {
        return this.getAll(null);
    }

    public List<InstanceJupyterSession> getAll(Pagination pagination) {
        final TypedQuery<InstanceJupyterSession> query = getEntityManager().createNamedQuery("instanceJupyterSession.getAll", InstanceJupyterSession.class);
        if (pagination != null) {
            final int offset = pagination.getOffset();
            final int limit = pagination.getLimit();
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    public List<InstanceJupyterSession> getAllByInstance(final Instance instance) {
        final TypedQuery<InstanceJupyterSession> query = getEntityManager().createNamedQuery("instanceJupyterSession.getAllByInstance", InstanceJupyterSession.class);
        query.setParameter("instance", instance);

        return query.getResultList();
    }

    public List<InstanceJupyterSession> getAllByInstanceKernelSession(final Instance instance, final String kernelId, final String sessionId) {
        final TypedQuery<InstanceJupyterSession> query = getEntityManager().createNamedQuery("instanceJupyterSession.getByInstanceKernelSession", InstanceJupyterSession.class);
        query.setParameter("instance", instance);
        query.setParameter("kernelId", kernelId);
        query.setParameter("sessionId", sessionId);
        return query.getResultList();
    }

    public void save(final InstanceJupyterSession instanceJupyterSession) {
        if (instanceJupyterSession.getId() == null) {
            persist(instanceJupyterSession);
        } else {
            merge(instanceJupyterSession);
        }
    }

    public Long countAll() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("instanceJupyterSession.countAll", Long.class);
        return query.getSingleResult();
    }

    public Long countAllInstances() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("instanceJupyterSession.countAllInstances", Long.class);
        return query.getSingleResult();
    }
}
