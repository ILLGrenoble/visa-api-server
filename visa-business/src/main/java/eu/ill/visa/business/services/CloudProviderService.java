package eu.ill.visa.business.services;

import eu.ill.visa.cloud.ProviderConfiguration;
import eu.ill.visa.cloud.ProviderConfigurationImpl;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.persistence.repositories.CloudProviderRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Singleton
public class CloudProviderService {

    private final CloudProviderRepository repository;
    private final CloudClientGateway cloudClientGateway;

    @Inject
    public CloudProviderService(final CloudProviderRepository repository,
                                final CloudClientGateway cloudClientGateway) {
        this.repository = repository;
        this.cloudClientGateway = cloudClientGateway;

        this.init();
    }

    private void init() {
        List<CloudProviderConfiguration> cloudProviderConfigurations = this.getAll();
        for (CloudProviderConfiguration cloudProviderConfiguration : cloudProviderConfigurations) {
            this.addCloudClient(cloudProviderConfiguration);
        }
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

        this.cloudClientGateway.removeCloudClient(cloudProviderConfiguration.getId());
    }

    public CloudClient save(@NotNull CloudProviderConfiguration cloudProviderConfiguration) {
        this.repository.save(cloudProviderConfiguration);

        return this.addCloudClient(cloudProviderConfiguration);
    }

    public CloudClient createCloudClient(CloudProviderConfiguration cloudProviderConfiguration) {
        this.repository.create(cloudProviderConfiguration);
        return this.addCloudClient(cloudProviderConfiguration);
    }

    private ProviderConfiguration convert(CloudProviderConfiguration cloudProviderConfiguration) {
        ProviderConfigurationImpl providerConfiguration = new ProviderConfigurationImpl();
        providerConfiguration.setName(cloudProviderConfiguration.getType());
        Map<String, String> parameters = new HashMap<>();
        for (CloudProviderConfiguration.CloudProviderConfigurationParameter configurationParameter : cloudProviderConfiguration.getParameters()) {
            parameters.put(configurationParameter.getKey(), configurationParameter.getValue());
        }

        providerConfiguration.setParameters(parameters);

        return providerConfiguration;
    }

    private CloudClient addCloudClient(CloudProviderConfiguration cloudProviderConfiguration) {
        return this.cloudClientGateway.addCloudClient(cloudProviderConfiguration.getId(), cloudProviderConfiguration.getName(), this.convert(cloudProviderConfiguration), cloudProviderConfiguration.getServerNamePrefix(), cloudProviderConfiguration.isVisible());
    }
}
