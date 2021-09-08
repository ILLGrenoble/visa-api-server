package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.FlavourLimitRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class FlavourLimitService {

    private FlavourLimitRepository repository;

    @Inject
    public FlavourLimitService(final FlavourLimitRepository repository) {
        this.repository = repository;
    }

    public List<FlavourLimit> getAll() {
        return this.repository.getAll();
    }

    public FlavourLimit getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(FlavourLimit FlavourLimit) {
        this.repository.delete(FlavourLimit);
    }

    public void save(@NotNull FlavourLimit flavourLimit) {
        this.repository.save(flavourLimit);
    }

    public List<FlavourLimit> getAll(Pagination pagination) {
        return this.getAll(new QueryFilter(), null, pagination);
    }

    public List<FlavourLimit> getAll(OrderBy orderBy, Pagination pagination) {
        return this.getAll(new QueryFilter(), orderBy, pagination);
    }

    public List<FlavourLimit> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<FlavourLimit> getAll(QueryFilter filter, Pagination pagination) {
        return this.repository.getAll(filter, null, pagination);
    }

    public Long countAll() {
        return repository.countAll(new QueryFilter());
    }

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }
}
