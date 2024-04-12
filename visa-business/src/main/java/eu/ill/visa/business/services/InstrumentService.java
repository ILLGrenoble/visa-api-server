package eu.ill.visa.business.services;


import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import eu.ill.visa.core.domain.Experiment;
import eu.ill.visa.core.domain.Instrument;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.persistence.repositories.InstrumentRepository;

import jakarta.validation.constraints.NotNull;
import java.util.List;


@Transactional
@Singleton
public class InstrumentService {

    private final InstrumentRepository repository;

    @Inject
    public InstrumentService(final InstrumentRepository repository) {
        this.repository = repository;
    }

    public List<Instrument> getAll() {
        return repository.getAll();
    }

    public List<Instrument> getAllForUser(@NotNull  final User user) {
        return repository.getAllForUser(user);
    }

    public Instrument getById(@NotNull final Long id) {
        return repository.getById(id);
    }

    public List<Instrument> getAllForExperimentsAndInstrumentScientist(List<Experiment> experiments, User user) {
        return repository.getAllForExperimentsAndInstrumentScientist(experiments, user);
    }
}
