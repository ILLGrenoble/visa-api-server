package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.Instrument;
import eu.ill.visa.core.entity.Plan;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.PlanRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class PlanService {

    private final PlanRepository repository;

    @Inject
    public PlanService(PlanRepository repository) {
        this.repository = repository;
    }

    public List<Plan> getAll() {
        return this.repository.getAll();
    }

    public Plan getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(Plan Plan) {
        this.repository.delete(Plan);
    }

    public void create(Plan plan) {
        this.repository.create(plan);
    }

    public void save(@NotNull Plan plan) {
        this.repository.save(plan);
    }

    public List<Plan> getAllForAdmin() {
        return repository.getAllForAdmin();
    }

    public List<Plan> getAllForInstruments(final List<Instrument> instruments) {
        return repository.getAllForInstruments(requireNonNullElseGet(instruments, ArrayList::new));
    }

    public List<Plan> getAllForExperiments(final List<Experiment> experiments$) {
        return repository.getAllForExperiments(requireNonNullElseGet(experiments$, ArrayList::new));
    }

    public List<Plan> getAllForUserAndExperiments(final User user, final List<Experiment> experiments$) {
        return repository.getAllForUserAndExperiments(user, requireNonNullElseGet(experiments$, ArrayList::new));
    }

    public List<Plan> getAllForUserAndAllInstruments(final User user) {
        return repository.getAllForUserAndAllInstruments(user);
    }
}
