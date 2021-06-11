package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.providers.UserFilterProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class UserRepository extends AbstractRepository<User> {

    @Inject
    public UserRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<User> getAll() {
        final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getAll", User.class);
        return query.getResultList();
    }

    public List<User> getAllActivated() {
        final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getAllActivated", User.class);
        return query.getResultList();
    }

    public User getById(String id) {
        try {
            final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getById", User.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<User> getAllLikeLastName(String lastName) {
        return getAllLikeLastName(lastName, null);
    }

    public List<User> getAllLikeLastName(String lastName, Pagination pagination) {
        final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getAllLikeLastName", User.class);
        query.setParameter("lastName", lastName);
        if (pagination != null) {
            query.setFirstResult(pagination.getOffset());
            query.setMaxResults(pagination.getLimit());
        }
        return query.getResultList();
    }

    public Long  countAllLikeLastName(String lastName) {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("user.countAllLikeLastName", Long.class);
            query.setParameter("lastName", lastName);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<User> getAllStaff() {
        final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getAllStaff", User.class);
        return query.getResultList();
    }


    public List<User> getAllSupport() {
        final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getAllSupport", User.class);
        return query.getResultList();
    }

    public List<User> getExperimentalTeamForInstance(Instance instance) {
        final TypedQuery<User> query = getEntityManager()
            .createNamedQuery("user.getExperimentalTeamForInstance", User.class);
        query.setParameter(1, instance.getId());
        return query.getResultList();
    }

    public void save(User user) {
        if (user.getId() == null) {
            persist(user);

        } else {
            merge(user);
        }
    }

    public Long countAll() {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("user.countAll", Long.class);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }



    public Long countAllUsersForRole(String role) {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("user.countAllUsersForRole", Long.class);
            query.setParameter("role", role);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<User> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final UserFilterProvider provider = new UserFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        final UserFilterProvider provider = new UserFilterProvider(getEntityManager());
        return super.countAll(provider, filter);
    }

    public Long countAllActivated() {
        TypedQuery<Long> query = getEntityManager().createNamedQuery("user.countAllActivated", Long.class);
        return query.getSingleResult();
    }
}
