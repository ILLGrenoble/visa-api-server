package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.providers.UserFilterProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class UserRepository extends AbstractRepository<User> {

    @Inject
    public UserRepository(final EntityManager entityManager) {
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

    public User getByIdWithRoles(String id) {
        try {
            final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getByIdWithRoles", User.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<User> getAllLikeLastName(String lastName, boolean onlyActivatedUsers) {
        return getAllLikeLastName(lastName, onlyActivatedUsers, null);
    }

    public List<User> getAllLikeLastName(String lastName, boolean onlyActivatedUsers, Pagination pagination) {
        final TypedQuery<User> query = onlyActivatedUsers
            ? getEntityManager().createNamedQuery("user.getAllActivatedLikeLastName", User.class)
            : getEntityManager().createNamedQuery("user.getAllLikeLastName", User.class);
        query.setParameter("lastName", lastName);
        if (pagination != null) {
            query.setFirstResult(pagination.getOffset());
            query.setMaxResults(pagination.getLimit());
        }
        return query.getResultList();
    }

    public Long  countAllLikeLastName(String lastName, boolean onlyActivatedUsers) {
        try {
            final TypedQuery<Long> query = onlyActivatedUsers
                ? getEntityManager().createNamedQuery("user.countAllLikeLastName", Long.class)
                : getEntityManager().createNamedQuery("user.countAllActivatedLikeLastName", Long.class);
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
