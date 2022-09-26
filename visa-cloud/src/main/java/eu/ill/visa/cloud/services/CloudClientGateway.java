package eu.ill.visa.cloud.services;

import eu.ill.visa.cloud.CloudConfiguration;
import eu.ill.visa.cloud.exceptions.CloudException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudClientGateway {
    private static final Logger logger = LoggerFactory.getLogger(CloudClientGateway.class);

    private CloudClient defaultCloudClient;

    public CloudClientGateway(final CloudConfiguration configuration) {
        final CloudClientFactory factory = new CloudClientFactory();
        try {
            this.defaultCloudClient = factory.getClient(configuration);

        } catch (CloudException e) {
            logger.error("Failed to create default Cloud Provider: {}", e.getMessage());
            e.printStackTrace();
        }

    }

    public CloudClient getDefaultCloudClient() {
        return defaultCloudClient;
    }
}
