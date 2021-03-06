package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.ExperimentRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class ExperimentService {

    @Inject
    private ExperimentRepository repository;

    public Experiment getById(@NotNull final String id) {
        return repository.getById(id);
    }

    public List<Experiment> getAll() {
        return repository.getAll();
    }

    public List<Integer> getYearsForUser(@NotNull final User user) {
        return repository.getYearsForUser(user);
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
                                          @NotNull final OrderBy orderBy) {
        return repository.getAllForUser(user, filter, pagination, orderBy);

    }

    public Experiment getByIdAndUser(@NotNull String id, @NotNull final User user) {
        return repository.getByIdAndUser(id, user);
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

