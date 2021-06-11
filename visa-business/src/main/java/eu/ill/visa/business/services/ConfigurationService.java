package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Configuration;
import eu.ill.visa.persistence.repositories.ConfigurationRepository;

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
