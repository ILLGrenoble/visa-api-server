package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.filters.UserFilter;
import eu.ill.visa.core.entity.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Singleton
public class UserRepository extends AbstractRepository<User> {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    @Inject
    public UserRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<User> getAll() {
        final TypedQuery<User> query = getEntityManager().createNamedQuery("user.getAll", User.class);
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

    public List<User> getAll(final UserFilter filter, final OrderBy orderBy, final Pagination pagination) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<User> cbQuery = cb.createQuery(User.class);
        final Root<User> root = cbQuery.from(User.class);

        Fetch<User, Employer> employerFetch = root.fetch("affiliation", JoinType.LEFT);

        UserRequestContext context = new UserRequestContext(root);
        final List<Predicate> predicates = this.convertFilterToPredicates(filter, cb, context);
        cbQuery.where(cb.and(predicates.toArray(new Predicate[0]))).distinct(true);

        if (orderBy != null) {
            if (orderBy.getName().equals("id")) {
                Path<Long> idOrder = root.get("id");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(idOrder) : cb.desc(idOrder));

            } else if (orderBy.getName().equals("activatedAt")) {
                Path<Date> activatedAtPath = root.get("activatedAt");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(activatedAtPath) : cb.desc(activatedAtPath));

            } else if (orderBy.getName().equals("lastSeenAt")) {
                Path<Date> lastSeenAtPath = root.get("lastSeenAt");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(lastSeenAtPath) : cb.desc(lastSeenAtPath));

            } else if (orderBy.getName().equals("lastName")) {
                Path<String> lastNameOrder = root.get("lastName");
                Path<String> firstNameOrder = root.get("firstName");
                List<Order> orderBys = List.of(
                    orderBy.getAscending() ? cb.asc(lastNameOrder) : cb.desc(lastNameOrder),
                    orderBy.getAscending() ? cb.asc(firstNameOrder) : cb.desc(firstNameOrder)
                );
                cbQuery.orderBy(orderBys);
            } else {
                logger.warn("Client has requested ordering of users by unknown field: {}", orderBy.getName());
            }
        }

        TypedQuery<User> query = getEntityManager().createQuery(cbQuery);

        if (pagination != null) {
            query.setFirstResult(pagination.getOffset());
            query.setMaxResults(pagination.getLimit());
        }

        return query.getResultList();
    }

    public Long countAll(final UserFilter filter) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Long> cbQuery = cb.createQuery(Long.class);
        final Root<User> root = cbQuery.from(User.class);

        UserRequestContext context = new UserRequestContext(root);
        final List<Predicate> predicates = this.convertFilterToPredicates(filter, cb, context);

        cbQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        cbQuery.select(cb.countDistinct(root));
        TypedQuery<Long> query = getEntityManager().createQuery(cbQuery);
        return query.getSingleResult();
    }

    protected List<Predicate> convertFilterToPredicates(final UserFilter filter,
                                                        final CriteriaBuilder cb,
                                                        final UserRequestContext context) {

        final Root<User> root = context.getRoot();

        final List<Predicate> predicates = new ArrayList<>();

        if (filter.getId() != null) {
            predicates.add(cb.equal(root.get("id"), filter.getId()));
        }

        if (filter.getRole() != null) {
            predicates.add(cb.equal(context.getRoleJoin().get("name"), filter.getRole()));
        }

        if (filter.getActivated() != null) {
            Predicate activatedPredicate = Boolean.TRUE.equals(filter.getActivated())
                ? cb.isNotNull(root.get("activatedAt"))
                : cb.isNull(root.get("activatedAt"));
            predicates.add(activatedPredicate);
        }

        return predicates;
    }

    public Long countAllActivated() {
        TypedQuery<Long> query = getEntityManager().createNamedQuery("user.countAllActivated", Long.class);
        return query.getSingleResult();
    }


    protected static final class UserRequestContext {
        private final Root<User> root;
        private Join<User, UserRole> userRoleJoin = null;
        private Join<UserRole, Role> roleJoin = null;

        public UserRequestContext(Root<User> root) {
            this.root = root;
        }

        public Root<User> getRoot() {
            return root;
        }

        public Join<User, UserRole> getUserRoleJoin() {
            if (userRoleJoin == null) {
                userRoleJoin = root.join("userRoles", JoinType.LEFT);
            }
            return userRoleJoin;
        }

        public Join<UserRole, Role> getRoleJoin() {
            if (roleJoin == null) {
                roleJoin = this.getUserRoleJoin().join("role", JoinType.LEFT);
            }
            return roleJoin;
        }

    }
}
