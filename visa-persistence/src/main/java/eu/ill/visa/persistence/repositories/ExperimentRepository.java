package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.ExperimentFilter;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class ExperimentRepository extends AbstractRepository<Experiment> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    @Inject
    ExperimentRepository(final EntityManager entityManager) {
        super(entityManager);
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

    public Set<Experiment> getAllForInstanceId(final Long instanceId) {
        final TypedQuery<Experiment> query = getEntityManager().createNamedQuery("experiment.getAllForInstanceId", Experiment.class);
        query.setParameter("instanceId", instanceId);

        return new HashSet<>(query.getResultList());
    }

    public List<Experiment> getAll(final ExperimentFilter filter) {
        return getAll(filter, null, null);
    }

    public List<Experiment> getAll(final ExperimentFilter filter, final Pagination pagination) {
        return getAll(filter, pagination, null);
    }

    public List<Experiment> getAll(final ExperimentFilter filter, Pagination pagination, OrderBy orderBy) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Experiment> cbQuery = cb.createQuery(Experiment.class);
        final Root<Experiment> root = cbQuery.from(Experiment.class);
        root.fetch("instrument", JoinType.INNER); // make sure instruments are selected so that the order by works correctly
        root.fetch("proposal", JoinType.INNER); // make sure proposals are selected so that the order by works correctly

        final List<Predicate> predicates = this.getExperimentPredicates(filter, cb, root);

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

    public Long countAll(ExperimentFilter filter) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Long> cbQuery = cb.createQuery(Long.class);
        final Root<Experiment> root = cbQuery.from(Experiment.class);

        final List<Predicate> predicates = this.getExperimentPredicates(filter, cb, root);

        cbQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        cbQuery.select(cb.countDistinct(root));
        TypedQuery<Long> query = getEntityManager().createQuery(cbQuery);
        return query.getSingleResult();

    }

    private List<Predicate> getExperimentPredicates(final ExperimentFilter filter,
                                                    final CriteriaBuilder cb,
                                                    final Root<Experiment> root) {
        final List<Predicate> predicates = new ArrayList<>();


        if (filter != null) {
            final Date startDate = filter.getStartDate() == null ? null : filter.getStartDate().getDate();
            final Date endDate = filter.getEndDate() == null ? null : filter.getEndDate().getDate();;
            final Set<String> proposals = filter.getProposals() == null || filter.getProposals().isEmpty() ? null : filter.getProposals();
            final Set<String> dois = filter.getDois() == null || filter.getDois().isEmpty() ? null : filter.getDois();

            Predicate userPredicate = null;
            Predicate openDataPredicate = null;
            if (filter.getUserId() != null) {
                userPredicate = cb.equal(root.get("users").get("id"), filter.getUserId());
            }

            if (filter.getIncludeOpenData()) {
                openDataPredicate = cb.lessThanOrEqualTo(root.get("proposal").get("publicAt"), new Date());
            }

            if (openDataPredicate != null && userPredicate != null) {
                predicates.add(cb.or(userPredicate, openDataPredicate));

            } else if (userPredicate != null) {
                predicates.add(userPredicate);

            } else if (openDataPredicate != null) {
                predicates.add(openDataPredicate);
            }

            if (filter.getInstrumentId() != null) {
                predicates.add(cb.equal(root.get("instrument").get("id"), filter.getInstrumentId()));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), endDate));
            }

            if (filter.getProposalLike() != null) {
                String identifierLike = String.format("%s%%", filter.getProposalLike()).toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("proposal").get("identifier")), identifierLike));
            }

            if (proposals != null && dois != null) {
                Predicate proposalIdentifierPredicate = root.get("proposal").get("identifier").in(proposals);
                Predicate proposalDOIPredicate = root.get("proposal").get("doi").in(dois);
                Predicate experimentDOIPredicate = root.get("doi").in(dois);
                predicates.add(cb.or(proposalIdentifierPredicate, proposalDOIPredicate, experimentDOIPredicate));

            } else if (proposals != null) {
                predicates.add(root.get("proposal").get("identifier").in(proposals));

            } else if (dois != null) {
                Predicate proposalDOIPredicate = root.get("proposal").get("doi").in(dois);
                Predicate experimentDOIPredicate = root.get("doi").in(dois);
                predicates.add(cb.or(proposalDOIPredicate, experimentDOIPredicate));
            }
        }


        return predicates;
    }
}
