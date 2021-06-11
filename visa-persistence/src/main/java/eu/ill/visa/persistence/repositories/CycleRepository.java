package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.providers.CycleFilterProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Singleton
public class CycleRepository extends AbstractRepository<Cycle> {

    @Inject
    public CycleRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<Cycle> getAll() {
        final TypedQuery<Cycle> query = getEntityManager().createNamedQuery("cycle.getAll", Cycle.class);
        return query.getResultList();
    }


    public List<Cycle> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final CycleFilterProvider provider = new CycleFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        final CycleFilterProvider provider = new CycleFilterProvider(getEntityManager());
        return super.countAll(provider, filter);
    }

    public Cycle getById(final Long id) {
        try {
            final TypedQuery<Cycle> query = getEntityManager().createNamedQuery("cycle.getById", Cycle.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public Cycle getCurrent() {
        try {
            final TypedQuery<Cycle> query = getEntityManager().createNamedQuery("cycle.getCurrent", Cycle.class);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<Cycle> getAll(User user) {
        final TypedQuery<Cycle> query = getEntityManager().createNamedQuery("cycle.getAllForUser", Cycle.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public Cycle getActiveCycleForDate(Date date) {
        try {
            final TypedQuery<Cycle> query = getEntityManager().createNamedQuery("cycle.getForDate", Cycle.class);
            query.setParameter("date", date);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }
}
