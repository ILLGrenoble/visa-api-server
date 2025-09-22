package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.CloudConfiguration;
import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.CloudProvider;
import eu.ill.visa.cloud.providers.openstack.endpoints.*;
import eu.ill.visa.cloud.providers.openstack.http.requests.ServerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class OpenStackProvider implements CloudProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackProvider.class);
    private static final int FLAVOUR_REFRESH_TIME_MINUTES = 5;

    private final OpenStackProviderConfiguration openStackConfiguration;

    private final ImageEndpoint imageEndpoint;
    private final ComputeEndpoint computeEndpoint;
    private final NetworkEndpoint networkEndpoint;

    private Map<CloudFlavour, List<CloudDeviceAllocation>> cloudFlavourDeviceAllocations;
    private Instant flavoursUpdateTime = Instant.MIN;

    public OpenStackProvider(final CloudConfiguration cloudConfiguration,
                             final OpenStackProviderConfiguration openStackConfiguration) {
        this.openStackConfiguration = openStackConfiguration;
        OpenStackIdentityEndpoint identityEndpoint = new OpenStackIdentityEndpoint(cloudConfiguration, this.openStackConfiguration);
        this.imageEndpoint = OpenStackImageEndpoint.authenticationProxy(cloudConfiguration, this.openStackConfiguration, identityEndpoint);
        this.networkEndpoint = OpenStackNetworkEndpoint.authenticationProxy(cloudConfiguration, this.openStackConfiguration, identityEndpoint);
        this.computeEndpoint = OpenStackComputeEndpoint.authenticationProxy(cloudConfiguration, openStackConfiguration, identityEndpoint);
    }

    public OpenStackProviderConfiguration getOpenStackConfiguration() {
        return openStackConfiguration;
    }

    @Override
    public List<CloudImage> images() throws CloudException {
        return this.imageEndpoint.images();
    }

    @Override
    public CloudImage image(final String id) throws CloudException {
        return this.imageEndpoint.image(id);
    }

    private synchronized Map<CloudFlavour, List<CloudDeviceAllocation>> getCloudFlavourDeviceAllocations() throws CloudException {
        if (Duration.between(this.flavoursUpdateTime, Instant.now()).toMinutes() > FLAVOUR_REFRESH_TIME_MINUTES) {
            this.cloudFlavourDeviceAllocations = new LinkedHashMap<>();
            final List<CloudFlavour> flavours = this.computeEndpoint.flavors();
            for  (CloudFlavour flavour : flavours) {
                final List<CloudDeviceAllocation> cloudDeviceAllocations = this.computeEndpoint.flavorDeviceAllocations(flavour.getId());
                this.cloudFlavourDeviceAllocations.put(flavour, cloudDeviceAllocations);
            }
            this.flavoursUpdateTime = Instant.now();
        }
        return this.cloudFlavourDeviceAllocations;
    }

    @Override
    public List<CloudFlavour> flavors() throws CloudException {
        return this.getCloudFlavourDeviceAllocations().keySet().stream().toList();
    }

    @Override
    public CloudFlavour flavor(final String id) throws CloudException {
        return this.getCloudFlavourDeviceAllocations().keySet().stream()
            .filter(flavour -> flavour.getId().equals(id))
            .findFirst().orElse(null);
    }

    @Override
    public List<CloudDevice> devices() throws CloudException {
        return this.getCloudFlavourDeviceAllocations().values().stream()
            .flatMap(List::stream)
            .map(CloudDeviceAllocation::getDevice)
            .distinct()
            .toList();
    }

    @Override
    public List<CloudDeviceAllocation> deviceAllocations() throws CloudException {
        return this.getCloudFlavourDeviceAllocations().values().stream()
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    @Override
    public CloudDevice device(String identifier, CloudDevice.Type deviceType) throws CloudException {
        return this.devices().stream()
            .filter(device -> device.getIdentifier().equals(identifier) && device.getType().equals(deviceType))
            .findFirst().orElse(null);
    }


    @Override
    public List<CloudDeviceAllocation> flavorDeviceAllocations(String flavourId) throws CloudException {
        return this.getCloudFlavourDeviceAllocations().entrySet().stream()
            .filter(entry -> entry.getKey().getId().equals(flavourId))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(new ArrayList<>());
    }

    @Override
    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        return this.computeEndpoint.instanceIdentifiers();
    }

    @Override
    public List<CloudInstance> instances() throws CloudException {
        return this.computeEndpoint.instances();
    }

    @Override
    public CloudInstance instance(final String id) throws CloudException {
        return this.computeEndpoint.instance(id);
    }

    @Override
    public String ip(final String id) throws CloudException {
        final CloudInstance instance = this.instance(id);
        return instance.getAddress();
    }

    @Override
    public void rebootInstance(String id) throws CloudException {
        this.computeEndpoint.rebootInstance(id);
    }

    @Override
    public void startInstance(String id) throws CloudException {
        this.computeEndpoint.startInstance(id);
    }

    @Override
    public void shutdownInstance(String id) throws CloudException {
        this.computeEndpoint.shutdownInstance(id);
    }

    @Override
    public void updateSecurityGroups(String id, List<String> securityGroupNames) throws CloudException {

        final List<String> currentSecurityGroupNames =this.computeEndpoint.serverSecurityGroups(id);
        List<String> securityGroupNamesToRemove = currentSecurityGroupNames.stream().filter(name -> !securityGroupNames.contains(name)).toList();
        List<String> securityGroupNamesToAdd = securityGroupNames.stream().filter(name -> !currentSecurityGroupNames.contains(name)).toList();

        logger.info("Updating instance {} security groups: removing [{}] and adding [{}]", id, String.join(", ", securityGroupNamesToRemove), String.join(", ", securityGroupNamesToAdd));

        // Remove obsolete security groups one by one
        for (String securityGroupName : securityGroupNamesToRemove) {
            this.computeEndpoint.removeServerSecurityGroup(id, securityGroupName);
        }

        // Add missing security groups one by one
        for (String securityGroupName : securityGroupNamesToAdd) {
            this.computeEndpoint.addServerSecurityGroup(id, securityGroupName);
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
            .networks(List.of(openStackConfiguration.getAddressProviderUUID()))
            .metadata(metadata)
            .userData(bootCommand)
            .build();

        String id = this.computeEndpoint.createInstance(serverInput);
        return this.instance(id);
    }

    @Override
    public void deleteInstance(String id) throws CloudException {
        this.computeEndpoint.deleteInstance(id);
    }

    @Override
    public CloudLimit limits() throws CloudException {
        return this.computeEndpoint.limits();
    }

    @Override
    public List<String> securityGroups() throws CloudException {
        return this.networkEndpoint.securityGroups();
    }
}
