package eu.ill.visa.persistence.repositories;

import eu.ill.preql.FilterQuery;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceJupyterSession;
import eu.ill.visa.persistence.providers.InstanceJupyterSessionFilterProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Singleton
public class InstanceJupyterSessionRepository extends AbstractRepository<InstanceJupyterSession> {

    @Inject
    InstanceJupyterSessionRepository(final EntityManager entityManager) {
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


    public List<InstanceJupyterSession> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final InstanceJupyterSessionFilterProvider provider = new InstanceJupyterSessionFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        final InstanceJupyterSessionFilterProvider provider = new InstanceJupyterSessionFilterProvider(getEntityManager());
        final FilterQuery<InstanceJupyterSession> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), null, null);
        query.addExpression((criteriaBuilder, root) ->
            criteriaBuilder.equal(root.get("active"), true)
        );
        return query.count();
    }

    public Long countAllInstances() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("instanceJupyterSession.countAllInstances", Long.class);
        return query.getSingleResult();
    }
}
