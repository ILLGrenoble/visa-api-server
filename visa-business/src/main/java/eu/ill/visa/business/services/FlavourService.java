package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.persistence.repositories.FlavourRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class FlavourService {

    private final FlavourRepository repository;

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

    public void save(@NotNull Flavour flavour) {
        this.repository.save(flavour);
    }

    public void create(Flavour flavour) {
        this.repository.create(flavour);
    }

    public Long countAllForAdmin() {
        return repository.countAllForAdmin();
    }

}
