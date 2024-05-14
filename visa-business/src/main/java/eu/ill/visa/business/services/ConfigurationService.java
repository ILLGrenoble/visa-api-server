package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Configuration;
import eu.ill.visa.persistence.repositories.ConfigurationRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@Singleton
public class ConfigurationService {

    private final ConfigurationRepository repository;

    @Inject
    public ConfigurationService(final ConfigurationRepository repository) {
        this.repository = repository;
    }

    public List<Configuration> getAll() {
        return repository.getAll();
    }

}
