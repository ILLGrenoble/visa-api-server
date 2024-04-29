package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.Instrument;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.providers.ExperimentFilterProvider;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@ApplicationScoped
public class ExperimentRepository extends AbstractRepository<Experiment> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    @Inject
    ExperimentRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Experiment> getAll() {
        final TypedQuery<Experiment> query = getEntityManager().createNamedQuery("experiment.getAll", Experiment.class);
        return query.getResultList();
    }

    public List<Experiment> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final ExperimentFilterProvider provider = new ExperimentFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        final ExperimentFilterProvider provider = new ExperimentFilterProvider(getEntityManager());
        return super.countAll(provider, filter);
    }

    public Experiment getById(final String id) {
        try {
            final TypedQuery<Experiment> query = getEntityManager().createNamedQuery("experiment.getById", Experiment.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<Integer> getYearsForUser(final User user) {
        final TypedQuery<Integer> query = getEntityManager().createNamedQuery("experiment.getYearsForUser", Integer.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<Integer> getYearsForOpenData() {
        final TypedQuery<Integer> query = getEntityManager().createNamedQuery("experiment.getYearsForOpenData", Integer.class);
        query.setParameter("currentDate", new Date());
        return query.getResultList();
    }

    public List<Experiment> getAllForUser(final User user) {
        return getAllForUser(user, null);
    }

    public List<Experiment> getAllForUser(final User user, final ExperimentFilter filter) {
        return getAllForUser(user, filter, null, null);
    }

    public List<Experiment> getAllForUser(final User user, final ExperimentFilter filter, final Pagination pagination) {
        return getAllForUser(user, filter, pagination, null);
    }

    public Experiment getByIdAndUser(final String id, final User user) {
        try {
            final TypedQuery<Experiment> query = getEntityManager().createNamedQuery("experiment.getByIdAndUser", Experiment.class);
            query.setParameter("id", id);
            query.setParameter("user", user);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public Experiment getByIdForOpenData(final String id) {
        try {
            final TypedQuery<Experiment> query = getEntityManager().createNamedQuery("experiment.getByIdForOpenData", Experiment.class);
            query.setParameter("id", id);
            query.setParameter("currentDate", new Date());
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public Set<Experiment> getAllForInstance(final Instance instance) {
        final TypedQuery<Experiment> query = getEntityManager().createNamedQuery("experiment.getAllForInstance", Experiment.class);
        query.setParameter("instance", instance);

        return new HashSet<>(query.getResultList());
    }

    public List<Experiment> getAllForUser(final User user, final ExperimentFilter filter, Pagination pagination, OrderBy orderBy) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Experiment> cbQuery = cb.createQuery(Experiment.class);
        final Root<Experiment> root = cbQuery.from(Experiment.class);
        root.fetch("instrument", JoinType.INNER); // make sure instruments are selected so that the order by works correctly
        root.fetch("proposal", JoinType.INNER); // make sure proposals are selected so that the order by works correctly

        final List<Predicate> predicates = this.getExperimentPredicates(user, filter, cb, root);

        cbQuery.where(cb.and(predicates.toArray(new Predicate[0]))).distinct(true);

        if (orderBy != null) {
            Path orderByPath = null;
            if (orderBy.getName().equals("date")) {
                orderByPath = root.get("startDate");
            } else if (orderBy.getName().equals("instrument")) {
                orderByPath = root.get("instrument").get("name");
            } else if (orderBy.getName().equals("proposal")) {
                orderByPath = root.get("proposal").get("identifier");
            }

            if (orderByPath != null) {
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(orderByPath) : cb.desc(orderByPath));
            } else {
                logger.warn("Client has requested ordering of experiments by unknown field: {}", orderBy.getName());
            }
        }

        TypedQuery<Experiment> query = getEntityManager().createQuery(cbQuery);

        if (pagination != null) {
            final int offset = pagination.getOffset();
            final int limit = pagination.getLimit();
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    public Long getAllCountForUser(User user, ExperimentFilter filter) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Long> cbQuery = cb.createQuery(Long.class);
        final Root<Experiment> root = cbQuery.from(Experiment.class);

        final List<Predicate> predicates = this.getExperimentPredicates(user, filter, cb, root);

        cbQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        cbQuery.select(cb.countDistinct(root));
        TypedQuery<Long> query = getEntityManager().createQuery(cbQuery);
        return query.getSingleResult();

    }

    private List<Predicate> getExperimentPredicates(final User user,
                                                    final ExperimentFilter filter,
                                                    final CriteriaBuilder cb,
                                                    final Root<Experiment> root) {
        final List<Predicate> predicates = new ArrayList<>();

        Boolean includeOpenData = false;

        if (filter != null) {
            final Instrument instrument = filter.getInstrument();
            final Date startDate = filter.getStartDate();
            final Date endDate = filter.getEndDate();
            final Set<String> proposalIdentifiers = filter.getProposalIdentifiers();
            final Set<String> dois = filter.getDois();
            includeOpenData = filter.getIncludeOpenData();

            if (instrument != null) {
                predicates.add(cb.equal(root.get("instrument"), instrument));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), endDate));
            }

            if (proposalIdentifiers != null && dois != null) {
                Predicate proposalIdentifierPredicate = root.get("proposal").get("identifier").in(proposalIdentifiers);
                Predicate proposalDOIPredicate = root.get("proposal").get("doi").in(dois);
                Predicate experimentDOIPredicate = root.get("doi").in(dois);
                predicates.add(cb.or(proposalIdentifierPredicate, proposalDOIPredicate, experimentDOIPredicate));

            } else if (proposalIdentifiers != null) {
                predicates.add(root.get("proposal").get("identifier").in(proposalIdentifiers));

            } else if (dois != null) {
                Predicate proposalDOIPredicate = root.get("proposal").get("doi").in(dois);
                Predicate experimentDOIPredicate = root.get("doi").in(dois);
                predicates.add(cb.or(proposalDOIPredicate, experimentDOIPredicate));
            }
        }

        Predicate userPredicate = cb.equal(root.join("users"), user);
        if (includeOpenData) {
            Predicate openDataPredicate = cb.lessThanOrEqualTo(root.get("proposal").get("publicAt"), new Date());
            predicates.add(cb.or(userPredicate, openDataPredicate));

        } else {
            predicates.add(userPredicate);
        }

        return predicates;
    }
}
