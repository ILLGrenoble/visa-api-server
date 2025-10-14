package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.FlavourRoleLifetime;
import eu.ill.visa.persistence.repositories.FlavourRoleLifetimeRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class FlavourRoleLifetimeService {

    private final FlavourRoleLifetimeRepository repository;

    @Inject
    public FlavourRoleLifetimeService(FlavourRoleLifetimeRepository repository) {
        this.repository = repository;
    }

    public List<FlavourRoleLifetime> getAll() {
        return this.repository.getAll();
    }

    public List<FlavourRoleLifetime> getAllByFlavourId(Long flavourId) {
        return this.repository.getAllByFlavourId(flavourId);
    }

    public List<List<FlavourRoleLifetime>> getAllByFlavourIds(final List<Long> flavourIds) {
        List<FlavourRoleLifetime> ungroupedFlavourRoleLifetimes = this.repository.getAllByFlavourIds(flavourIds);
        return flavourIds.stream().map(id -> {
            return ungroupedFlavourRoleLifetimes.stream().filter(flavourRoleLifetime -> flavourRoleLifetime.getFlavour().getId().equals(id)).toList();
        }).toList();
    }


    public FlavourRoleLifetime getById(Long id) {
        return this.repository.getById(id);
    }

    public void setRoleLifeTimesForFlavour(Flavour flavour, List<FlavourRoleLifetime> requiredRoleLifetimes) {
        final List<FlavourRoleLifetime> currentRoleLifetimes = this.getAllByFlavourId(flavour.getId());


        List<FlavourRoleLifetime> toRemove = currentRoleLifetimes.stream()
            .filter(currentRoleLifetime -> requiredRoleLifetimes.stream()
                .noneMatch(requiredRoleLifetime -> currentRoleLifetime.getId().equals(requiredRoleLifetime.getId())))
            .toList();

        requiredRoleLifetimes.forEach(this::save);
        toRemove.forEach(this::delete);
    }

    public void save(@NotNull FlavourRoleLifetime flavourRoleLifetime) {
        this.repository.save(flavourRoleLifetime);
    }

    public void delete(@NotNull FlavourRoleLifetime flavourRoleLifetime) {
        this.repository.delete(flavourRoleLifetime);
    }
}
