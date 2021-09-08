package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.providers.FlavourLimitFilterProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class FlavourLimitRepository extends AbstractRepository<FlavourLimit> {

    @Inject
    FlavourLimitRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<FlavourLimit> getAll() {
        final TypedQuery<FlavourLimit> query = getEntityManager().createNamedQuery("flavourLimit.getAll", FlavourLimit.class);
        return query.getResultList();
    }

    public List<FlavourLimit> getAll(Pagination pagination) {
        return this.getAll(null, null, pagination);
    }

    public List<FlavourLimit> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final FlavourLimitFilterProvider provider = new FlavourLimitFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy, pagination);
    }

    public Long countAll() {
        return this.countAll(null);
    }

    public Long countAll(QueryFilter filter) {
        final FlavourLimitFilterProvider provider = new FlavourLimitFilterProvider(getEntityManager());
        return super.countAll(provider, filter);
    }


    public FlavourLimit getById(final Long id) {
        try {
            final TypedQuery<FlavourLimit> query = getEntityManager().createNamedQuery("flavourLimit.getById", FlavourLimit.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(final FlavourLimit flavourLimit) {
        remove(flavourLimit);
    }

    public void save(final FlavourLimit flavourLimit) {
        if (flavourLimit.getId() == null) {
            persist(flavourLimit);
        } else {
            merge(flavourLimit);
        }
    }
}
