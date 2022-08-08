package eu.ill.visa.cloud.services;

import com.google.inject.Singleton;
import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.CloudProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class CloudClient {

    private final static Logger        logger = LoggerFactory.getLogger(CloudClient.class);
    private final        CloudProvider provider;
    private final        String        serverNamePrefix;

    public CloudClient(final CloudProvider provider, final String serverNamePrefix) {
        this.provider = provider;
        this.serverNamePrefix = serverNamePrefix;
    }

    public List<CloudImage> images() throws CloudException {
        logger.info("Fetching cloud images");
        return provider.images();
    }

    public CloudImage image(String id) throws CloudException {
        logger.info("Fetching image with id: {}", id);
        return provider.image(id);
    }

    public List<CloudFlavour> flavours() throws CloudException {
        logger.info("Fetching cloud flavours");
        return provider.flavors();
    }

    public CloudFlavour flavour(String id) throws CloudException {
        logger.info("Fetching flavour with id: {}", id);
        return provider.flavor(id);
    }

    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        logger.info("Fetching cloud instance identifiers");
        return provider.instanceIdentifiers();
    }

    public List<CloudInstance> instances() throws CloudException {
        logger.info("Fetching cloud instances");
        return provider.instances();
    }

    public CloudInstance instance(final String id) throws CloudException {
        if (id == null) {
            return null;
        }
        logger.info("Fetching cloud instance with id: {}", id);
        return provider.instance(id);
    }

    public void rebootInstance(final String id) throws CloudException {
        logger.info("Rebooting cloud instance with id: {}", id);
        provider.rebootInstance(id);
    }

    public void shutdownInstance(final String id) throws CloudException {
        logger.info("Shutting down cloud instance with id: {}", id);
        provider.shutdownInstance(id);
    }

    public void startInstance(final String id) throws CloudException {
        logger.info("Starting cloud instance with id: {}", id);
        provider.startInstance(id);
    }

    public void updateSecurityGroups(final String id, final List<String> securityGroupNames) throws CloudException {
        logger.info("Updating security groups for instance with id: {}", id);
        provider.updateSecurityGroups(id, securityGroupNames);
    }

    public CloudInstance createInstance(final String name,
                                        final String imageId,
                                        final String flavorId,
                                        final List<String> securityGroupNames,
                                        final CloudInstanceMetadata metadata,
                                        final String bootCommand,
                                        final List<String> networkProviders) throws CloudException {
        logger.info("Creating instance with name: {}", name);
        return provider.createInstance(name,
            imageId,
            flavorId,
            securityGroupNames,
            metadata,
            bootCommand,
            networkProviders
        );
    }

    public void deleteInstance(final String id) throws CloudException {
        logger.info("Deleting instance with id: {}", id);
        provider.deleteInstance(id);
    }

    public String getServerNamePrefix() {
        return serverNamePrefix;
    }

    public CloudLimit limits() throws CloudException {
        return provider.limits();
    }

    public String ip(final String id) throws CloudException {
        return provider.ip(id);
    }

    public List<String> securityGroups() throws CloudException {
        logger.info("Fetching cloud security groups");
        return provider.securityGroups();
    }

    public List<CloudNetwork> networks() throws CloudException {
        logger.info("Fetching cloud networks");
        return provider.networks();
    }

    public CloudNetwork network(final String id) throws CloudException {
        logger.info("Fetching cloud network: {}", id);
        return provider.network(id);
    }

}
