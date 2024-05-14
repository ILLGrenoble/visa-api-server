package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.ExperimentFilter;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.ExperimentRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNullElseGet;

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

    public List<Experiment> getAll() {
        return repository.getAll();
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

    public List<Experiment> getAllForUser(@NotNull final User user) {
        return repository.getAllForUser(user);
    }

    public List<Experiment> getAllForUser(@NotNull final User user, @NotNull final ExperimentFilter filter) {
        return repository.getAllForUser(user, filter);
    }

    public List<Experiment> getAllForUser(@NotNull final User user, @NotNull final ExperimentFilter filter, @NotNull final Pagination pagination) {
        return repository.getAllForUser(user, filter, pagination);
    }

    public List<Experiment> getAllForUser(@NotNull final User user,
                                          @NotNull final ExperimentFilter filter,
                                          @NotNull final Pagination pagination,
                                          final OrderBy orderBy) {
        return repository.getAllForUser(user, filter, pagination, orderBy);

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
        return repository.getAllForInstance(instance);
    }

    public Long getAllCountForUser(@NotNull final User user, @NotNull final ExperimentFilter filter) {
        return repository.getAllCountForUser(user, filter);
    }

    public Long getAllCountForUser(@NotNull final User user) {
        return repository.getAllCountForUser(user, null);
    }

    public List<Experiment> getAll(OrderBy orderBy, Pagination pagination) {
        return this.getAll(new QueryFilter(), orderBy, pagination);
    }

    public List<Experiment> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<Experiment> getAll(QueryFilter filter, Pagination pagination) {
        return this.repository.getAll(filter, null, pagination);
    }

    public Long countAll() {
        return repository.countAll(new QueryFilter());
    }

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }

}

