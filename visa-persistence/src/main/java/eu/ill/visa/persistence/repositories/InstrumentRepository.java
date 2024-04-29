package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.Instrument;
import eu.ill.visa.core.entity.User;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class InstrumentRepository extends AbstractRepository<Instrument> {

    @Inject
    InstrumentRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Instrument> getAll() {
        final TypedQuery<Instrument> query = getEntityManager().createNamedQuery("instrument.getAll", Instrument.class);
        return query.getResultList();
    }

    public Instrument getById(final Long id) {
        try {
            final TypedQuery<Instrument> query = getEntityManager().createNamedQuery("instrument.getById", Instrument.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<Instrument> getAllForUser(final User user) {
        final TypedQuery<Instrument> query = getEntityManager().createNamedQuery("instrument.getAllForUser", Instrument.class);
        query.setParameter("user", user);
        return query.getResultList();
    }


    public List<Instrument> getAllForExperimentsAndInstrumentScientist(List<Experiment> experiments, User user) {
        final TypedQuery<Instrument> query;
        if (experiments == null || experiments.size() == 0) {
            query = getEntityManager().createNamedQuery("instrument.getAllForInstrumentScientist", Instrument.class);

        } else {
            query = getEntityManager().createNamedQuery("instrument.getAllForExperimentsAndInstrumentScientist", Instrument.class);
            List<String> experimentsIds = experiments.stream().map(Experiment::getId).collect(Collectors.toList());
            query.setParameter("experimentIds", experimentsIds);

        }
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }
}
