package eu.ill.visa.business.services;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.FlavourRepository;

import jakarta.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class FlavourService {

    private FlavourRepository repository;

    @Inject
    public FlavourService(FlavourRepository repository) {
        this.repository = repository;
    }

    public List<Flavour> getAll() {
        return this.repository.getAll();
    }

    public List<Flavour> getAllForAdmin() {
        return this.repository.getAllForAdmin();
    }

    public Flavour getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(Flavour flavour) {
        this.repository.delete(flavour);
    }

    public void save(@NotNull Flavour flavour) {
        this.repository.save(flavour);
    }

    public void create(Flavour flavour) {
        this.repository.create(flavour);
    }

    public List<Flavour> getAll(OrderBy orderBy, Pagination pagination) {
        return this.getAll(new QueryFilter(), orderBy, pagination);
    }

    public List<Flavour> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<Flavour> getAll( Pagination pagination) {
        return this.repository.getAll( pagination);
    }

    public Long countAll() {
        return repository.countAll(new QueryFilter());
    }

    public Long countAllForAdmin() {
        return repository.countAllForAdmin();
    }

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }


}
