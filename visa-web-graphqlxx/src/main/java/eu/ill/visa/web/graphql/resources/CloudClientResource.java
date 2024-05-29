package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.*;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class CloudClientResource {

    private final CloudClientGateway cloudClientGateway;

    @Inject
    public CloudClientResource(final CloudClientGateway cloudClientGateway) {
        this.cloudClientGateway = cloudClientGateway;
    }

    /**
     * Get cloud clients
     *
     * @return a list of cloud clients
     */
    @Query
    public List<CloudClientType> cloudClients() {
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
    public CompletableFuture<List<CloudImageType>> cloudImages(@AdaptToScalar(Scalar.Int.class) Long cloudId) {
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
    public CompletableFuture<List<CloudFlavourType>> cloudFlavours(@AdaptToScalar(Scalar.Int.class) Long cloudId) {
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
    public CompletableFuture<List<DetailedCloudLimit>> cloudLimits() {
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
    public CompletableFuture<List<CloudSecurityGroupType>> cloudSecurityGroups(@AdaptToScalar(Scalar.Int.class) Long cloudId) {
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

    private CloudClient getCloudClient(@AdaptToScalar(Scalar.Int.class) Long cloudId) throws DataFetchingException {
        CloudClient cloudClient = this.cloudClientGateway.getCloudClient(cloudId);
        if (cloudClient == null) {
            throw new DataFetchingException("Cloud Client with ID " + cloudId + " does not exist");
        }

        return cloudClient;
    }
}
