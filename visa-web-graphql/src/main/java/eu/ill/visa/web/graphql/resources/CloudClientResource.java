package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.business.services.DevicePoolService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudUnavailableException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientFactory;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import eu.ill.visa.core.entity.partial.NumberInstancesByCloudClient;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.CloudClientInput;
import eu.ill.visa.web.graphql.inputs.OpenStackProviderConfigurationInput;
import eu.ill.visa.web.graphql.inputs.WebProviderConfigurationInput;
import eu.ill.visa.web.graphql.types.*;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class CloudClientResource {
    private static final Logger logger = LoggerFactory.getLogger(CloudClientResource.class);

    private final CloudClientService cloudClientService;
    private final InstanceService instanceService;
    private final DevicePoolService devicePoolService;

    @Inject
    public CloudClientResource(final CloudClientService cloudClientService,
                               final InstanceService instanceService,
                               final DevicePoolService devicePoolService) {
        this.cloudClientService = cloudClientService;
        this.instanceService = instanceService;
        this.devicePoolService = devicePoolService;
    }

    /**
     * Get cloud clients
     *
     * @return a list of cloud clients
     */
    @Query
    public @NotNull List<CloudClientType> cloudClients() {
        return this.cloudClientService.getAll().stream()
            .map(CloudClientType::new)
            .toList();
    }


    /**
     * Get cloud images from the the cloud provider
     *
     * @return a list of cloud images
     */
    @Query
    public @NotNull List<CloudImageType> cloudImages(@AdaptToScalar(Scalar.Int.class) @NotNull Long cloudId) throws DataFetchingException {
        try {
            CloudClient cloudClient = this.getCloudClient(cloudId);
            return cloudClient.images().stream().map(CloudImageType::new).toList();
        } catch (DataFetchingException e) {
            throw e;
        } catch (Exception exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get cloud flavours from the the cloud provider
     *
     * @return a list of cloud flavours
     */
    @Query
    public @NotNull List<CloudFlavourType> cloudFlavours(@AdaptToScalar(Scalar.Int.class) Long cloudId) throws DataFetchingException {
        try {
            CloudClient cloudClient = this.getCloudClient(cloudId);
            return cloudClient.flavours().stream().map(cloudFlavour -> new CloudFlavourType(cloudFlavour, cloudClient)).toList();
        } catch (DataFetchingException e) {
            throw e;
        } catch (Exception exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get cloud devices from the the cloud provider
     *
     * @return a list of cloud devices
     */
    @Query
    public @NotNull List<CloudDeviceType> cloudDevices(@AdaptToScalar(Scalar.Int.class) Long cloudId) throws DataFetchingException {
        try {
            CloudClient cloudClient = this.getCloudClient(cloudId);
            return cloudClient.devices().stream().map(CloudDeviceType::new).toList();
        } catch (DataFetchingException e) {
            throw e;
        } catch (Exception exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get custom cloud resource classes from the the cloud provider (if available)
     *
     * @return a response containing a list of cloud resource classes if available
     */
    @Query
    public @NotNull PlacementArrayResponse<String> cloudResourceClasses(@AdaptToScalar(Scalar.Int.class) Long cloudId) throws DataFetchingException {
        try {
            CloudClient cloudClient = this.getCloudClient(cloudId);
            return PlacementArrayResponse.Response(cloudClient.resourceClasses());
        } catch (CloudUnavailableException e) {
            return PlacementArrayResponse.Unavailable();
        } catch (DataFetchingException e) {
            throw e;
        } catch (Exception exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Get cloud limits from the the cloud provider
     *
     * @return a list of cloud limits
     */
    @Query
    public @NotNull List<DetailedCloudLimit> cloudLimits() {
        List<CloudClient> cloudClients = this.cloudClientService.getAll();
        List<DevicePoolUsage> devicePoolUsages = this.devicePoolService.getDevicePoolUsage();

        return cloudClients.stream().map(cloudClient -> {
            try {
                CloudLimit cloudLimit = cloudClient.limits();
                Long cloudId = cloudClient.getId() == null || cloudClient.getId() == -1 ? null : cloudClient.getId();
                List<DevicePoolUsage> cloudDevicePoolUsage = devicePoolUsages.stream().filter(devicePoolUsage -> devicePoolUsage.getCloudId().equals(cloudId)).toList();

                return new DetailedCloudLimit(new CloudClientType(cloudClient), cloudLimit, cloudDevicePoolUsage);
            } catch (Exception exception) {
                logger.warn("Failed to get cloud limits for {}", cloudClient.getId());
                return new DetailedCloudLimit(new CloudClientType(cloudClient), exception.getMessage());
            }
        }).toList();
    }

    /**
     * Get cloud security groups from the the cloud provider
     *
     * @return a list of security groups
     */
    @Query
    public @NotNull List<CloudSecurityGroupType> cloudSecurityGroups(@AdaptToScalar(Scalar.Int.class) Long cloudId) throws DataFetchingException {
        try {
            CloudClient cloudClient = this.getCloudClient(cloudId);
            return cloudClient.securityGroups().stream()
                .map(CloudSecurityGroupType::new)
                .toList();
        } catch (DataFetchingException e) {
            throw e;
        } catch (Exception exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }


    /**
     * Create a new cloudClient
     *
     * @param input the cloudClient properties
     * @return the newly created cloudClient
     */
    @Mutation
    public @NotNull CloudClientType createCloudClient(@NotNull @Valid CloudClientInput input) throws InvalidInputException {
        CloudProviderConfiguration.Builder builder = new CloudProviderConfiguration.Builder();
        CloudProviderConfiguration configuration = builder
            .type(input.getType())
            .name(input.getName())
            .visible(input.getVisible())
            .serverNamePrefix(input.getServerNamePrefix())
            .build();

        this.setCloudConfigurationParameters(configuration, input);

        return new CloudClientType(this.cloudClientService.createOrUpdateCloudClient(configuration));
    }

    /**
     * Update a cloudClient
     *
     * @param id    the cloudClient id
     * @param input the cloudClient properties
     * @return the updated created cloudClient
     * @throws EntityNotFoundException thrown if the given the cloudClient id was not found
     */
    @Mutation
    public @NotNull CloudClientType updateCloudClient(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull @Valid CloudClientInput input) throws EntityNotFoundException, InvalidInputException  {
        if (id == -1) {
            throw new InvalidInputException("The default cloud provider cannot be modified");
        }

        CloudProviderConfiguration configuration = this.cloudClientService.getCloudProviderConfiguration(id);
        if (configuration == null) {
            throw new EntityNotFoundException("Cloud provider not found for the given id");
        }

        if (!configuration.getType().equals(input.getType())) {
            configuration.deleteParameters();
        }

        configuration.setName(input.getName());
        configuration.setServerNamePrefix(input.getServerNamePrefix());
        configuration.setType(input.getType());
        configuration.setVisible(input.getVisible());

        this.setCloudConfigurationParameters(configuration, input);

        return new CloudClientType(this.cloudClientService.createOrUpdateCloudClient(configuration));
    }

    /**
     * Delete a cloudClient for a given id
     *
     * @param id the cloudClient id
     * @return true if deleted
     * @throws EntityNotFoundException thrown if the cloudClient is not found
     * @throws InvalidInputException thrown if trying to delete the default cloud client
     */
    @Mutation
    public @NotNull Boolean deleteCloudClient(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException, InvalidInputException {
        if (id == -1) {
            throw new InvalidInputException("The default cloud client cannot be deleted");
        }

        final CloudClient cloudClient = this.cloudClientService.getCloudClient(id);
        if (cloudClient == null) {
            throw new EntityNotFoundException("Cloud Client not found for the given id");
        }

        NumberInstancesByCloudClient counter = this.instanceService.countByCloudClient().stream().filter(count -> {
            if (count.getId() == null) {
                return id == -1L;
            } else {
                return count.getId().equals(id);
            }
        }).findFirst().orElse(null);
        if (counter != null && counter.getTotal() > 0) {
            throw new InvalidInputException("Cannot delete a cloud provider with active instances");
        }

        this.cloudClientService.delete(cloudClient);
        return true;
    }

    private void setCloudConfigurationParameters(CloudProviderConfiguration cloudProviderConfiguration, CloudClientInput input) throws InvalidInputException {
        if (input.getType().equals(CloudClientFactory.OPENSTACK)) {
            if (input.getOpenStackProviderConfiguration() == null) {
                throw new InvalidInputException("OpenStack provider configuration must be specified");
            }
            OpenStackProviderConfigurationInput configurationInput = input.getOpenStackProviderConfiguration();

            cloudProviderConfiguration.setParameter("applicationId", configurationInput.getApplicationId());
            cloudProviderConfiguration.setParameter("applicationSecret", configurationInput.getApplicationSecret());
            cloudProviderConfiguration.setParameter("computeEndpoint", configurationInput.getComputeEndpoint());
            cloudProviderConfiguration.setParameter("placementEndpoint", configurationInput.getPlacementEndpoint());
            cloudProviderConfiguration.setParameter("imageEndpoint", configurationInput.getImageEndpoint());
            cloudProviderConfiguration.setParameter("networkEndpoint", configurationInput.getNetworkEndpoint());
            cloudProviderConfiguration.setParameter("identityEndpoint", configurationInput.getIdentityEndpoint());
            cloudProviderConfiguration.setParameter("addressProvider", configurationInput.getAddressProvider());
            cloudProviderConfiguration.setParameter("addressProviderUUID", configurationInput.getAddressProviderUUID());

        } else if (input.getType().equals(CloudClientFactory.WEB)) {
            if (input.getWebProviderConfiguration() == null) {
                throw new InvalidInputException("Web provider configuration must be specified");
            }

            WebProviderConfigurationInput configurationInput = input.getWebProviderConfiguration();
            cloudProviderConfiguration.setParameter("url", configurationInput.getUrl());
            cloudProviderConfiguration.setParameter("authToken", configurationInput.getAuthToken());

        } else {
            throw new InvalidInputException("Cloud provider type must be specified");
        }
    }

    private CloudClient getCloudClient(Long cloudId) throws DataFetchingException {
        CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudId);
        if (cloudClient == null) {
            throw new DataFetchingException("Cloud Client with ID " + cloudId + " does not exist");
        }

        return cloudClient;
    }
}
