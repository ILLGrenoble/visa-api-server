package eu.ill.visa.cloud.services;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudUnavailableException;
import eu.ill.visa.cloud.providers.CloudProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CloudClient {

    private final static Logger        logger = LoggerFactory.getLogger(CloudClient.class);

    private final        Long          id;
    private final        String        name;
    private final        String        type;
    private final        CloudProvider provider;
    private final        String        serverNamePrefix;
    private final        Boolean       visible;


    public CloudClient(final Long id,
                       final String name,
                       final String type,
                       final CloudProvider provider,
                       final String serverNamePrefix,
                       final Boolean visible) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.provider = provider;
        this.serverNamePrefix = serverNamePrefix;
        this.visible = visible;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getServerNamePrefix() {
        return serverNamePrefix;
    }

    public Boolean getVisible() {
        return visible;
    }

    public CloudProvider getProvider() {
        return provider;
    }

    public List<CloudImage> images() throws CloudException {
        logger.trace("Fetching cloud images");
        return provider.images();
    }

    public CloudImage image(String id) throws CloudException {
        logger.trace("Fetching image with id: {}", id);
        return provider.image(id);
    }

    public List<CloudFlavour> flavours() throws CloudException {
        logger.trace("Fetching cloud flavours");
        return provider.flavors();
    }

    public CloudFlavour flavour(String id) throws CloudException {
        logger.trace("Fetching flavour with id: {}", id);
        return provider.flavor(id);
    }

    public List<CloudDevice> devices() throws CloudException {
        logger.trace("Fetching cloud devices");
        return provider.devices();
    }

    public List<CloudDeviceAllocation> deviceAllocations() throws CloudException {
        logger.trace("Fetching cloud device allocations");
        return provider.deviceAllocations();
    }

    public CloudDevice device(String identifier, CloudDevice.Type deviceType) throws CloudException {
        logger.trace("Fetching cloud device with identifier: {} and type {}", identifier, deviceType);
        return provider.device(identifier, deviceType);
    }

    public List<CloudDeviceAllocation> flavourDeviceAllocations(String flavourId) throws CloudException {
        logger.trace("Fetching cloud devices for flavour with id: {}", flavourId);
        return provider.flavorDeviceAllocations(flavourId);
    }

    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        logger.trace("Fetching cloud instance identifiers");
        return provider.instanceIdentifiers();
    }

    public List<CloudInstance> instances() throws CloudException {
        logger.trace("Fetching cloud instances");
        return provider.instances();
    }

    public CloudInstance instance(final String id) throws CloudException {
        if (id == null) {
            return null;
        }
        logger.trace("Fetching cloud instance with id: {}", id);
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
                                        final String bootCommand) throws CloudException {
        logger.info("Creating instance with name: {}", name);
        return provider.createInstance(name, imageId, flavorId, securityGroupNames, metadata, bootCommand);
    }

    public void deleteInstance(final String id) throws CloudException {
        logger.info("Deleting instance with id: {}", id);
        provider.deleteInstance(id);
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

    public List<String> resourceClasses() throws CloudException, CloudUnavailableException {
        return provider.resourceClasses();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CloudClient that = (CloudClient) o;

        return new EqualsBuilder().append(id, that.id).append(name, that.name).append(serverNamePrefix, that.serverNamePrefix).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(name).append(serverNamePrefix).toHashCode();
    }
}
