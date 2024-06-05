package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.CloudProvider;
import eu.ill.visa.cloud.providers.openstack.http.requests.ServerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OpenStackProvider implements CloudProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackProvider.class);

    private final OpenStackProviderConfiguration configuration;

    private final OpenStackImageProvider imageProvider;
    private final OpenStackComputeProvider computeProvider;
    private final OpenStackNetworkProvider networkProvider;

    public OpenStackProvider(final OpenStackProviderConfiguration configuration) {
        this.configuration = configuration;
        OpenStackIdentityProvider identityProvider = new OpenStackIdentityProvider(this.configuration);
        this.imageProvider = new OpenStackImageProvider(this.configuration, identityProvider);
        this.computeProvider = new OpenStackComputeProvider(this.configuration, identityProvider);
        this.networkProvider = new OpenStackNetworkProvider(this.configuration, identityProvider);
    }

    public OpenStackProviderConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public List<CloudImage> images() throws CloudException {
        return this.imageProvider.images();
    }

    @Override
    public CloudImage image(final String id) throws CloudException {
        return this.imageProvider.image(id);
    }

    @Override
    public List<CloudFlavour> flavors() throws CloudException {
        return this.computeProvider.flavors();
    }

    @Override
    public CloudFlavour flavor(final String id) throws CloudException {
        return this.computeProvider.flavor(id);
    }

    @Override
    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        return this.computeProvider.instanceIdentifiers();
    }

    @Override
    public List<CloudInstance> instances() throws CloudException {
        return this.computeProvider.instances();
    }

    @Override
    public CloudInstance instance(final String id) throws CloudException {
        return this.computeProvider.instance(id);
    }

    @Override
    public String ip(final String id) throws CloudException {
        final CloudInstance instance = this.instance(id);
        return instance.getAddress();
    }

    @Override
    public void rebootInstance(String id) throws CloudException {
        this.computeProvider.rebootInstance(id);
    }

    @Override
    public void startInstance(String id) throws CloudException {
        this.computeProvider.startInstance(id);
    }

    @Override
    public void shutdownInstance(String id) throws CloudException {
        this.computeProvider.shutdownInstance(id);
    }

    @Override
    public void updateSecurityGroups(String id, List<String> securityGroupNames) throws CloudException {

        final List<String> currentSecurityGroupNames =this.computeProvider.serverSecurityGroups(id);
        List<String> securityGroupNamesToRemove = currentSecurityGroupNames.stream().filter(name -> !securityGroupNames.contains(name)).toList();
        List<String> securityGroupNamesToAdd = securityGroupNames.stream().filter(name -> !currentSecurityGroupNames.contains(name)).toList();

        logger.info("Updating instance {} security groups: removing [{}] and adding [{}]", id, String.join(", ", securityGroupNamesToRemove), String.join(", ", securityGroupNamesToAdd));

        // Remove obsolete security groups one by one
        for (String securityGroupName : securityGroupNamesToRemove) {
            this.computeProvider.removeServerSecurityGroup(id, securityGroupName);
        }

        // Add missing security groups one by one
        for (String securityGroupName : securityGroupNamesToAdd) {
            this.computeProvider.addServerSecurityGroup(id, securityGroupName);
        }
    }

    @Override
    public CloudInstance createInstance(final String name,
                                        final String imageId,
                                        final String flavorId,
                                        final List<String> securityGroupNames,
                                        final CloudInstanceMetadata metadata,
                                        final String bootCommand) throws CloudException {

        ServerInput serverInput = ServerInput.Builder()
            .name(name)
            .imageId(imageId)
            .flavorId(flavorId)
            .securityGroups(securityGroupNames)
            .networks(List.of(configuration.getAddressProviderUUID()))
            .metadata(metadata)
            .userData(bootCommand)
            .build();

        String id = this.computeProvider.createInstance(serverInput);
        return this.instance(id);
    }

    @Override
    public void deleteInstance(String id) throws CloudException {
        this.computeProvider.deleteInstance(id);
    }

    @Override
    public CloudLimit limits() throws CloudException {
        return this.computeProvider.limits();
    }

    @Override
    public List<String> securityGroups() throws CloudException {
        return this.networkProvider.securityGroups();
    }

}
