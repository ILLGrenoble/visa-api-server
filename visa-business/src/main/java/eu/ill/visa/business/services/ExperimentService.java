package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.ExperimentFilter;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.ExperimentRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.*;

@Transactional
@Singleton
public class ExperimentService {

    private final ExperimentRepository repository;

    @Inject
    public ExperimentService(final ExperimentRepository repository) {
        this.repository = repository;
    }

    public Experiment getById(@NotNull final String id) {
        return repository.getById(id);
    }

    public List<Integer> getYearsForUser(@NotNull final User user, Boolean includeOpenData) {
        List<Integer> userYears = repository.getYearsForUser(user);
        if (includeOpenData) {
            Set<Integer> openYears = new HashSet<>(repository.getYearsForOpenData());
            openYears.addAll(userYears);

            return new ArrayList<>(openYears);

        } else {
            return userYears;
        }
    }

    public List<Experiment> getAll(@NotNull final ExperimentFilter filter) {
        return repository.getAll(filter);
    }

    public List<Experiment> getAll(@NotNull final ExperimentFilter filter, @NotNull final Pagination pagination) {
        return repository.getAll(filter, pagination);
    }

    public List<Experiment> getAll(@NotNull final ExperimentFilter filter,
                                   @NotNull final Pagination pagination,
                                   final OrderBy orderBy) {
        return repository.getAll(filter, pagination, orderBy);

    }

    public Long countAll(@NotNull final ExperimentFilter filter) {
        return repository.countAll(filter);
    }

    public Experiment getByIdAndUser(@NotNull String id, @NotNull final User user) {
        return repository.getByIdAndUser(id, user);
    }

    public Experiment getByIdAndUser(@NotNull String id, @NotNull final User user, Boolean includeOpenData) {
        Experiment experiment = repository.getByIdAndUser(id, user);
        if (experiment == null && includeOpenData) {
            experiment = repository.getByIdForOpenData(id);
        }

        return experiment;
    }

    public Set<Experiment> getAllForInstance(final Instance instance) {
        return repository.getAllForInstanceId(instance.getId());
    }

    public Set<Experiment> getAllForInstanceId(Long id) {
        return repository.getAllForInstanceId(id);
    }

}

