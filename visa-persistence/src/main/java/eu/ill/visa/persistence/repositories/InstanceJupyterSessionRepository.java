package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceJupyterSession;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class InstanceJupyterSessionRepository extends AbstractRepository<InstanceJupyterSession> {

    @Inject
    InstanceJupyterSessionRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    public List<InstanceJupyterSession> getAll() {
        final TypedQuery<InstanceJupyterSession> query = getEntityManager().createNamedQuery("instanceJupyterSession.getAll", InstanceJupyterSession.class);
        return query.getResultList();
    }

    public List<InstanceJupyterSession> getAllByInstance(final Instance instance) {
        final TypedQuery<InstanceJupyterSession> query = getEntityManager().createNamedQuery("instanceJupyterSession.getAllByInstance", InstanceJupyterSession.class);
        query.setParameter("instance", instance);

        List<InstanceJupyterSession> sessions = query.getResultList();

        return sessions;
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
}
