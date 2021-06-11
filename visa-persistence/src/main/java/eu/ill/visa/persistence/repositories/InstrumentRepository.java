package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Experiment;
import eu.ill.visa.core.domain.Instrument;
import eu.ill.visa.core.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class InstrumentRepository extends AbstractRepository<Instrument> {

    @Inject
    InstrumentRepository(final Provider<EntityManager> entityManager) {
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
