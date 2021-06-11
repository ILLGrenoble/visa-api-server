package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.CycleRepository;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class CycleService {

    private final CycleRepository repository;

    @Inject
    public CycleService(final CycleRepository repository) {
        this.repository = repository;
    }

    public Cycle getById(@NotNull final Long id) {
        return repository.getById(id);
    }

    public Cycle getCurrent() {
        return repository.getCurrent();
    }

    public List<Cycle> getAll() {
        return repository.getAll();
    }

    public List<Cycle> getAll(OrderBy orderBy, Pagination pagination) {
        return this.getAll(new QueryFilter(), orderBy, pagination);
    }

    public List<Cycle> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<Cycle> getAll(QueryFilter filter, Pagination pagination) {
        return this.repository.getAll(filter, null, pagination);
    }

    public Long countAll() {
        return repository.countAll(new QueryFilter());
    }

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }

    public List<Cycle> getAllForUser(@NotNull final User user) {
        return repository.getAll(user);
    }

    public Cycle getActiveCycleForDate(Date date) {
        return repository.getActiveCycleForDate(date);
    }
}
