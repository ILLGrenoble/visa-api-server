package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.persistence.repositories.CloudProviderRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

@Transactional
@Singleton
public class CloudProviderService {

    private final CloudProviderRepository repository;

    @Inject
    public CloudProviderService(final CloudProviderRepository repository) {
        this.repository = repository;
    }

    public CloudProviderConfiguration getById(Long id) {
        return this.repository.getById(id);
    }

    public List<CloudProviderConfiguration> getAll() {
        return this.repository.getAll();
    }

    public void delete(CloudProviderConfiguration cloudProviderConfiguration) {
        cloudProviderConfiguration.setDeletedAt(new Date());
        this.repository.save(cloudProviderConfiguration);
    }

    public void save(@NotNull CloudProviderConfiguration cloudProviderConfiguration) {
        this.repository.save(cloudProviderConfiguration);
    }
}
