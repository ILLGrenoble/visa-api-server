package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.cloud.ProviderConfiguration;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.CloudProviderConfiguration;
import eu.ill.visa.persistence.repositories.CloudProviderRepository;

import javax.validation.constraints.NotNull;
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
            this.cloudClientGateway.addCloudClient(cloudProviderConfiguration.getId(), cloudProviderConfiguration.getName(), this.convert(cloudProviderConfiguration), cloudProviderConfiguration.getServerNamePrefix());
        }
    }

    public CloudProviderConfiguration getById(Long id) {
        return this.repository.getById(id);
    }

    public List<CloudProviderConfiguration> getAll() {
        return this.repository.getAll();
    }

    public void delete(CloudProviderConfiguration cloudProviderConfiguration) {
        this.repository.delete(cloudProviderConfiguration);

        this.cloudClientGateway.removeCloudClient(cloudProviderConfiguration.getId());
    }

    public void save(@NotNull CloudProviderConfiguration cloudProviderConfiguration) {
        this.repository.save(cloudProviderConfiguration);

        this.cloudClientGateway.updateCloudClient(cloudProviderConfiguration.getId(), cloudProviderConfiguration.getName(), this.convert(cloudProviderConfiguration), cloudProviderConfiguration.getServerNamePrefix());
    }

    public void create(CloudProviderConfiguration cloudProviderConfiguration) {
        this.repository.create(cloudProviderConfiguration);
        this.cloudClientGateway.addCloudClient(cloudProviderConfiguration.getId(), cloudProviderConfiguration.getName(), this.convert(cloudProviderConfiguration), cloudProviderConfiguration.getServerNamePrefix());
    }

    private ProviderConfiguration convert(CloudProviderConfiguration cloudProviderConfiguration) {
        ProviderConfiguration providerConfiguration = new ProviderConfiguration();
        providerConfiguration.setName(cloudProviderConfiguration.getType());
        Map<String, String> parameters = new HashMap<>();
        for (CloudProviderConfiguration.CloudProviderConfigurationParameter configurationParameter : cloudProviderConfiguration.getParameters()) {
            parameters.put(configurationParameter.getKey(), configurationParameter.getValue());
        }

        providerConfiguration.setParameters(parameters);

        return providerConfiguration;
    }

}
