package eu.ill.visa.cloud.providers.web;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.exceptions.CloudRuntimeException;
import eu.ill.visa.cloud.providers.CloudProvider;
import eu.ill.visa.cloud.providers.web.http.WebProviderClient;
import eu.ill.visa.cloud.providers.web.http.requests.InstanceSecurityGroupRequest;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * This provider enables VISA to another cloud provider i.e. proxmox, vmware etc.
 * It forwards requests to a web service implementation that encapsulates the underlying cloud provider
 */
public class WebProvider implements CloudProvider {

    private static final Logger logger = LoggerFactory.getLogger(WebProvider.class);

    private final WebProviderConfiguration configuration;
    private final WebProviderClient webProviderClient;

    public WebProvider(final WebProviderConfiguration configuration) {
        this.configuration = requireNonNull(configuration, "configuration cannot be null");

        this.webProviderClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getUrl()))
            .build(WebProviderClient.class);
    }

    public WebProviderConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        try {
            return this.webProviderClient.instanceIdentifiers(this.configuration.getAuthToken());

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud instance identifiers from Web Provider: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<CloudInstance> instances() throws CloudException {
        try {
            return this.webProviderClient.instances(this.configuration.getAuthToken());

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud instances from Web Provider: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public CloudInstance instance(final String id) throws CloudException {
        try {
            return this.webProviderClient.instance(this.configuration.getAuthToken(), id);

        } catch (CloudNotFoundException e) {
            return null;

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud instance with id {} from Web Provider: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public void updateSecurityGroups(final String id, final List<String> securityGroupNames) throws CloudException {
        try {
            List<String> currentSecurityGroupNames = this.webProviderClient.instanceSecurityGroups(this.configuration.getAuthToken(), id);

            final List<String> securityGroupNamesToRemove = currentSecurityGroupNames.stream().filter(name -> !securityGroupNames.contains(name)).toList();
            final List<String> securityGroupNamesToAdd = securityGroupNames.stream().filter(name -> !currentSecurityGroupNames.contains(name)).toList();

            logger.info("Updating instance {} security groups: removing [{}] and adding [{}]", id, String.join(", ", securityGroupNamesToRemove), String.join(", ", securityGroupNamesToAdd));

            // Remove obsolete security groups one by one
            for (String securityGroupName : securityGroupNamesToRemove) {
                try {
                    this.webProviderClient.removeInstanceSecurityGroup(this.configuration.getAuthToken(), id, new InstanceSecurityGroupRequest(securityGroupName));

                } catch (Exception e) {
                    logger.warn("Could not remove security group '{}' from the server id {} and response {}", securityGroupName, id, e.getMessage());
                }
            }

            // Add missing security groups one by one
            for (String securityGroupName : securityGroupNamesToAdd) {
                try {
                    this.webProviderClient.addInstanceSecurityGroup(this.configuration.getAuthToken(), id, new InstanceSecurityGroupRequest(securityGroupName));

                } catch (Exception e) {
                    logger.warn("Could not add security group '{}' to the server with id {} and response {}", securityGroupName, id, e.getMessage());
                }
            }

        } catch (CloudClientException e) {
            logger.warn("Could not get security groups from the server with id {} and response {}", id, e.getMessage());
        }
    }

    @Override
    public CloudInstance createInstance(final String name,
                                        final String imageId,
                                        final String flavorId,
                                        final List<String> securityGroupNames,
                                        final CloudInstanceMetadata metadata,
                                        final String bootCommand) throws CloudException {
        CloudInstance cloudInstance = CloudInstance.newBuilder()
            .name(name)
            .imageId(imageId)
            .flavorId(flavorId)
            .securityGroups(securityGroupNames)
            .metadata(metadata)
            .bootCommand(bootCommand)
            .build();

        try {
            final String id = this.webProviderClient.createInstance(this.configuration.getAuthToken(), cloudInstance).id();
            return this.instance(id);

        } catch (CloudClientException e) {
            throw new CloudException(format("Could not create server with name %s and response %s ", name, e.getMessage()));
        }
    }

    @Override
    public List<CloudImage> images() throws CloudException {
        try {
            return this.webProviderClient.images(this.configuration.getAuthToken());

        } catch (CloudClientException e) {
            logger.error("Failed to get cloud images from Web Provider: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public CloudImage image(final String id) throws CloudException {
        try {
            return this.webProviderClient.image(this.configuration.getAuthToken(), id);

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud image with id {} from Web Provider: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public List<CloudFlavour> flavors() throws CloudException {
        try {
            return this.webProviderClient.flavours(this.configuration.getAuthToken());

        } catch (CloudClientException e) {
            logger.error("Failed to get cloud images from Web Provider: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public CloudFlavour flavor(final String id) throws CloudException {
        try {
            return this.webProviderClient.flavour(this.configuration.getAuthToken(), id);

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud image with id {} from Web Provider: {}", id, e.getMessage());
            return null;
        }
    }

    @Override
    public String ip(String id) throws CloudException {
        final CloudInstance instance = instance(id);
        return instance.getAddress();
    }

    @Override
    public void rebootInstance(String id) throws CloudException {
        try {
            this.webProviderClient.rebootInstance(this.configuration.getAuthToken(), id);

        } catch (Exception e) {
            throw new CloudException(format("Could not reboot server with id %s and response %s: ", id, e.getMessage()));
        }
    }

    @Override
    public void shutdownInstance(String id) throws CloudException {
        try {
            this.webProviderClient.shutdownInstance(this.configuration.getAuthToken(), id);

        } catch (Exception e) {
            throw new CloudException(format("Could not shutdown server with id %s and response %s: ", id, e.getMessage()));
        }
    }

    @Override
    public void startInstance(String id) throws CloudException {
        try {
            this.webProviderClient.startInstance(this.configuration.getAuthToken(), id);

        } catch (Exception e) {
            throw new CloudException(format("Could not start server with id %s and response %s: ", id, e.getMessage()));
        }
    }

    @Override
    public void deleteInstance(String id) throws CloudException {
        try {
            this.webProviderClient.deleteInstance(this.configuration.getAuthToken(), id);

        } catch (Exception e) {
            throw new CloudException(format("Could not delete server with id %s and response %s: ", id, e.getMessage()));
        }
    }

    @Override
    public CloudLimit limits() throws CloudException {
        try {
            return this.webProviderClient.limits(this.configuration.getAuthToken());

        } catch (CloudRuntimeException e) {
            logger.warn("Failed to get cloud metrics from Web Provider: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            throw new CloudException("Failed to get cloud metrics from Web Provider " + e.getMessage());
        }
    }

    @Override
    public List<String> securityGroups() throws CloudException {
        try {
            List<String> securityGroups = this.webProviderClient.securityGroups(this.configuration.getAuthToken());
            return securityGroups;

        } catch (CloudRuntimeException e) {
            logger.warn("Failed to get security groups from Web Provider: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            throw new CloudException("Failed to get security groups from Web Provider " + e.getMessage());
        }
    }
}
