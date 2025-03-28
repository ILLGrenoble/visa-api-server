package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.FlavourLimit;
import eu.ill.visa.persistence.repositories.FlavourLimitRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class FlavourLimitService {

    private final FlavourLimitRepository repository;

    @Inject
    public FlavourLimitService(final FlavourLimitRepository repository) {
        this.repository = repository;
    }

    public List<FlavourLimit> getAll() {
        return this.repository.getAll();
    }

    public List<FlavourLimit> getAllOfTypeForFlavour(Flavour flavour, String type) {
        return this.repository.getAllOfTypeForFlavour(flavour, type);
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

}
