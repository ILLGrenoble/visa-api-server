package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.SecurityGroupFilter;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class SecurityGroupFilterRepository extends AbstractRepository<SecurityGroupFilter> {

    @Inject
    SecurityGroupFilterRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<SecurityGroupFilter> getAll() {
        final TypedQuery<SecurityGroupFilter> query = getEntityManager().createNamedQuery("securityGroupFilter.getAll", SecurityGroupFilter.class);
        return query.getResultList();
    }

    public List<SecurityGroupFilter> getAll(Pagination pagination) {
        final TypedQuery<SecurityGroupFilter> query = getEntityManager().createNamedQuery("securityGroupFilter.getAll", SecurityGroupFilter.class);
        if (pagination != null) {
            final int offset = pagination.getOffset();
            final int limit = pagination.getLimit();
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    public Long countAll() {
        TypedQuery<Long> query = getEntityManager().createNamedQuery("securityGroupFilter.countAll", Long.class);
        return query.getSingleResult();
    }

    public SecurityGroupFilter getById(Long id) {
        try {
            TypedQuery<SecurityGroupFilter> query = getEntityManager().createNamedQuery("securityGroupFilter.getById", SecurityGroupFilter.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public void delete(final SecurityGroupFilter securityGroupFilter) {
        remove(securityGroupFilter);
    }

    public void save(final SecurityGroupFilter securityGroupFilter) {
        if (securityGroupFilter.getId() == null) {
            persist(securityGroupFilter);
        } else {
            merge(securityGroupFilter);
        }
    }
}
