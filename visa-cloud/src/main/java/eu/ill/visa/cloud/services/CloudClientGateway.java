package eu.ill.visa.cloud.services;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.cloud.CloudConfiguration;
import eu.ill.visa.cloud.ProviderConfiguration;
import eu.ill.visa.cloud.exceptions.CloudException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CloudClientGateway {
    private static final Logger logger = LoggerFactory.getLogger(CloudClientGateway.class);
    private final CloudClientFactory factory = new CloudClientFactory();

    private CloudClient defaultCloudClient;
    private final Map<Long, CloudClient> secondaryCloudClients = new HashMap<>();

    @Inject
    public CloudClientGateway(final CloudConfiguration configuration) {
        try {
            this.defaultCloudClient = factory.getClient(configuration);
            this.secondaryCloudClients.put(this.defaultCloudClient.getId(), this.defaultCloudClient);

        } catch (CloudException e) {
            logger.error("Failed to create default Cloud Provider: {}", e.getMessage());
        }

    }

    public CloudClient getDefaultCloudClient() {
        return defaultCloudClient;
    }

    public CloudClient getCloudClient(Long id) {
        if (id == null) {
            return this.getDefaultCloudClient();
        }

        CloudClient cloudClient = this.secondaryCloudClients.get(id);

        if (cloudClient == null) {
            logger.error("Failed to get Cloud Client with ID {}", id);
        }

        return cloudClient;
    }

    public List<CloudClient> getAll() {
        return new ArrayList<>(this.secondaryCloudClients.values());
    }

    public CloudClient addCloudClient(Long providerId, String name, ProviderConfiguration providerConfiguration, String serverNamePrefix, boolean visible) {
        try {
            CloudClient cloudClient = this.factory.getClient(providerId, name, providerConfiguration.name(), providerConfiguration, serverNamePrefix, visible);
            this.secondaryCloudClients.put(providerId, cloudClient);

            return cloudClient;
        } catch (CloudException e) {
            logger.error("Failed to create default Cloud Provider: {}", e.getMessage());
        }

        return null;
    }

    public CloudClient updateCloudClient(Long providerId, String name, ProviderConfiguration providerConfiguration, String serverNamePrefix, boolean visible) {
        this.secondaryCloudClients.remove(providerId);

        return this.addCloudClient(providerId, name, providerConfiguration, serverNamePrefix, visible);
    }

    public void removeCloudClient(Long providerId) {
        this.secondaryCloudClients.remove(providerId);
    }
}
