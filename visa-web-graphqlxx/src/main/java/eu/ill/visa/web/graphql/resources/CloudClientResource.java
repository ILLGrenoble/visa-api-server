package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.CloudProviderService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientFactory;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.NumberInstancesByCloudClient;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.Role;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class CloudClientResource {

    private final CloudClientGateway cloudClientGateway;
    private final CloudProviderService cloudProviderService;
    private final InstanceService instanceService;

    @Inject
    public CloudClientResource(final CloudClientGateway cloudClientGateway,
                               final CloudProviderService cloudProviderService,
                               final InstanceService instanceService) {
        this.cloudClientGateway = cloudClientGateway;
        this.cloudProviderService = cloudProviderService;
        this.instanceService = instanceService;
    }

    /**
     * Get cloud clients
     *
     * @return a list of cloud clients
     */
    @Query
    public @NotNull List<CloudClientType> cloudClients() {
        return this.cloudClientGateway.getAll().stream()
            .map(CloudClientType::new)
            .toList();
    }


    /**
     * Get cloud images from the the cloud provider
     *
     * @return a list of cloud images
     */
    @Query
    public @NotNull CompletableFuture<List<CloudImageType>> cloudImages(@AdaptToScalar(Scalar.Int.class) @NotNull Long cloudId) {
        final CompletableFuture<List<CloudImageType>> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                CloudClient cloudClient = this.getCloudClient(cloudId);
                future.complete(cloudClient.images().stream().map(CloudImageType::new).toList());
            } catch (DataFetchingException e) {
                future.completeExceptionally(e);
            } catch (Exception exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    /**
     * Get cloud flavours from the the cloud provider
     *
     * @return a list of cloud flavours
     */
    @Query
    public @NotNull CompletableFuture<List<CloudFlavourType>> cloudFlavours(@AdaptToScalar(Scalar.Int.class) Long cloudId) {
        final CompletableFuture<List<CloudFlavourType>> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                CloudClient cloudClient = this.getCloudClient(cloudId);
                future.complete(cloudClient.flavours().stream().map(CloudFlavourType::new).toList());
            } catch (DataFetchingException e) {
                future.completeExceptionally(e);
            } catch (Exception exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    /**
     * Get cloud limits from the the cloud provider
     *
     * @return a list of cloud limits
     */
    @Query
    public @NotNull CompletableFuture<List<DetailedCloudLimit>> cloudLimits() {
        List<CloudClient> cloudClients = this.cloudClientGateway.getAll();

        List<CompletableFuture<DetailedCloudLimit>> allCloudLimitsFutures = cloudClients.stream().map(cloudClient -> {
            final CompletableFuture<DetailedCloudLimit> future = new CompletableFuture<>();
            runAsync(() -> {
                try {
                    CloudLimit cloudLimit = cloudClient.limits();
                    future.complete(new DetailedCloudLimit(new CloudClientType(cloudClient), cloudLimit));
                } catch (CloudException exception) {
                    future.complete(new DetailedCloudLimit(new CloudClientType(cloudClient), exception.getMessage()));
                }
            });
            return future;
        }).toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(allCloudLimitsFutures.toArray(new CompletableFuture[0]));

        return allFutures.thenApply(future -> allCloudLimitsFutures.stream().map(CompletableFuture::join).toList()).toCompletableFuture();
    }

    /**
     * Get cloud security groups from the the cloud provider
     *
     * @return a list of security groups
     */
    @Query
    public @NotNull CompletableFuture<List<CloudSecurityGroupType>> cloudSecurityGroups(@AdaptToScalar(Scalar.Int.class) Long cloudId) {
        final CompletableFuture<List<CloudSecurityGroupType>> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                CloudClient cloudClient = this.getCloudClient(cloudId);
                final List<CloudSecurityGroupType> cloudSecurityGroups = cloudClient.securityGroups().stream()
                    .map(CloudSecurityGroupType::new)
                    .toList();
                future.complete(cloudSecurityGroups);
            } catch (DataFetchingException e) {
                future.completeExceptionally(e);
            } catch (Exception exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
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

        return new CloudClientType(this.cloudProviderService.createCloudClient(configuration));
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

        CloudProviderConfiguration configuration = this.cloudProviderService.getById(id);
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

        return new CloudClientType(this.cloudProviderService.save(configuration));
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

        final CloudProviderConfiguration configuration = this.cloudProviderService.getById(id);
        if (configuration == null) {
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

        this.cloudProviderService.delete(configuration);
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
        CloudClient cloudClient = this.cloudClientGateway.getCloudClient(cloudId);
        if (cloudClient == null) {
            throw new DataFetchingException("Cloud Client with ID " + cloudId + " does not exist");
        }

        return cloudClient;
    }
}
