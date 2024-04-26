package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.providers.PlanFilterProvider;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PlanRepository extends AbstractRepository<Plan> {


    @Inject
    PlanRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Plan> getAll() {
        final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getAll", Plan.class);
        return query.getResultList();
    }

    public List<Plan> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final PlanFilterProvider provider = new PlanFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        final PlanFilterProvider provider = new PlanFilterProvider(getEntityManager());
        return super.countAll(provider, filter);
    }

    public Plan getById(final Long id) {
        try {
            final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getById", Plan.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(final Plan plan) {
        remove(plan);
    }

    public void create(Plan plan) {
        persist(plan);
    }

    public void save(final Plan plan) {
        if (plan.getId() == null) {
            persist(plan);

        } else {
            merge(plan);
        }
    }

    public List<Plan> getAllForAdmin() {
        final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getAllForAdmin", Plan.class);
        return query.getResultList();
    }

    public List<Plan> getAllForInstruments(final List<Instrument> instruments) {
        if (instruments == null || instruments.size() == 0) {
            return this.getAllForAllInstruments();

        } else {
            final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getAllForInstrumentIds", Plan.class);
            List<Long> instrumentIds = instruments.stream().map(Instrument::getId).collect(Collectors.toList());
            query.setParameter("instrumentIds", instrumentIds);

            return query.getResultList();
        }
    }

    public List<Plan> getAllForUserAndExperiments(final User user, final List<Experiment> experiments) {
        if (experiments == null || experiments.size() == 0) {
            return this.getAllForUserAndAllInstruments(user);

        } else if (user == null) {
            return this.getAllForExperiments(experiments);

        } else {
            final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getAllForUserAndExperimentIds", Plan.class);
            List<String> experimentIds = experiments.stream().map(Experiment::getId).collect(Collectors.toList());
            query.setParameter("user", user);
            query.setParameter("experimentIds", experimentIds);

            return query.getResultList();
        }
    }

    public List<Plan> getAllForExperiments(final List<Experiment> experiments) {
        if (experiments == null || experiments.size() == 0) {
            return this.getAllForAllInstruments();

        } else {
            final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getAllForExperimentIds", Plan.class);
            List<String> experimentIds = experiments.stream().map(Experiment::getId).collect(Collectors.toList());
            query.setParameter("experimentIds", experimentIds);

            return query.getResultList();
        }
    }

    public List<Plan> getAllForUserAndAllInstruments(final User user) {
        if (user == null) {
            return this.getAllForAllInstruments();

        } else {
            final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getAllForUserAndAllInstruments", Plan.class);
            query.setParameter("user", user);

            return query.getResultList();
        }
    }

    public List<Plan> getAllForAllInstruments() {
        final TypedQuery<Plan> query = getEntityManager().createNamedQuery("plan.getAllForUserAndAllInstruments", Plan.class);

        return query.getResultList();
    }
}
