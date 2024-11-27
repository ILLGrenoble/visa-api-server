package eu.ill.visa.business.services;

import eu.ill.visa.cloud.ProviderConfiguration;
import eu.ill.visa.cloud.ProviderConfigurationImpl;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Singleton
public class CloudClientService {

    private final CloudProviderService cloudProviderService;
    private final CloudClientGateway cloudClientGateway;

    public CloudClientService(final CloudProviderService cloudProviderService,
                              final CloudClientGateway cloudClientGateway) {
        this.cloudProviderService = cloudProviderService;
        this.cloudClientGateway = cloudClientGateway;
    }

    @Startup
    public void init() {
        List<CloudProviderConfiguration> cloudProviderConfigurations = this.cloudProviderService.getAll();
        for (CloudProviderConfiguration cloudProviderConfiguration : cloudProviderConfigurations) {
            this.addCloudClient(cloudProviderConfiguration);
        }
    }

    public List<CloudClient> getAll() {
        // Get all providers
        List<CloudProviderConfiguration> configurations = this.cloudProviderService.getAll();

        // Get all cloud clients
        List<CloudClient> clients = this.cloudClientGateway.getAll();

        // Check for changes
        List<CloudClient> deletedClients = clients.stream()
            .filter(cloudClient -> cloudClient.getId() != null && cloudClient.getId() != -1) // ignore default cloud client
            .filter(cloudClient -> configurations.stream()
                .map(CloudProviderConfiguration::getId)
                .filter(id -> cloudClient.getId().equals(id))
                .findAny()
                .isEmpty())
            .toList();

        List<CloudProviderConfiguration> newConfigurations = configurations.stream()
            .filter(configuration -> clients.stream()
                .map(CloudClient::getId)
                .filter(id -> configuration.getId().equals(id))
                .findAny()
                .isEmpty())
            .toList();

        for (CloudClient cloudClient : deletedClients) {
            this.cloudClientGateway.removeCloudClient(cloudClient.getId());
        }

        for (CloudProviderConfiguration configuration : newConfigurations) {
            this.addCloudClient(configuration);
        }

        return this.cloudClientGateway.getAll();
    }

    public CloudClient getCloudClient(Long id) {
        if (id == null || id == -1) {
            return this.cloudClientGateway.getCloudClient(null);
        }

        CloudProviderConfiguration configuration = this.cloudProviderService.getById(id);
        CloudClient cloudClient = this.cloudClientGateway.getCloudClient(id);

        if (configuration == null) {
            if (cloudClient != null) {
                this.cloudClientGateway.removeCloudClient(id);
            }
            return null;
        }

        if (cloudClient == null) {
            cloudClient = this.addCloudClient(configuration);
        }
        return cloudClient;
    }

    public List<CloudClient> getCloudClients(List<Long> ids) {
        return ids.stream().map(this::getCloudClient).toList();
    }

    public CloudProviderConfiguration getCloudProviderConfiguration(Long id) {
        return this.cloudProviderService.getById(id);
    }

    public CloudClient createOrUpdateCloudClient(CloudProviderConfiguration configuration) {
        this.cloudProviderService.save(configuration);
        return this.addCloudClient(configuration);
    }

    public void delete(CloudClient cloudClient) {
        CloudProviderConfiguration cloudProviderConfiguration = this.cloudProviderService.getById(cloudClient.getId());
        if (cloudProviderConfiguration != null) {
            this.cloudProviderService.delete(cloudProviderConfiguration);
        }
        this.cloudClientGateway.removeCloudClient(cloudClient.getId());
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
        return this.cloudClientGateway.addCloudClient(
            cloudProviderConfiguration.getId(),
            cloudProviderConfiguration.getName(),
            this.convert(cloudProviderConfiguration),
            cloudProviderConfiguration.getServerNamePrefix(),
            cloudProviderConfiguration.isVisible()
        );
    }
}
