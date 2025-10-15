package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.business.services.FlavourRoleLifetimeService;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.FlavourRoleLifetime;
import eu.ill.visa.web.graphql.types.*;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

@RegisterForReflection
@GraphQLApi
public class FlavourResolver {

    private final CloudClientService cloudClientService;
    private final FlavourRoleLifetimeService flavourRoleLifetimeService;

    public FlavourResolver(final CloudClientService cloudClientService,
                           final FlavourRoleLifetimeService flavourRoleLifetimeService) {
        this.cloudClientService = cloudClientService;
        this.flavourRoleLifetimeService = flavourRoleLifetimeService;
    }

    public List<CloudClientType> cloudClient(@Source List<FlavourType> flavours) {
        List<CloudClient> cloudClients = this.cloudClientService.getAll();
        return flavours.stream().map(flavour -> {
            return cloudClients.stream().filter(cloudClient -> {
                return cloudClient.getId() == -1 ? flavour.getCloudId() == null : cloudClient.getId().equals(flavour.getCloudId());
            }).findFirst().orElse(null);
        }).map(cloudClient -> cloudClient == null ? null : new CloudClientType(cloudClient)).toList();
    }

    public CloudFlavourType cloudFlavour(@Source FlavourType flavour) {
        try {
            CloudClient cloudClient = this.cloudClientService.getAll().stream().filter(aCloudClient -> {
                return aCloudClient.getId() == -1 ? flavour.getCloudId() == null : aCloudClient.getId().equals(flavour.getCloudId());
            }).findFirst().orElse(null);

            if (cloudClient != null) {
                CloudFlavour cloudFlavour = cloudClient.flavour(flavour.getComputeId());
                if (cloudFlavour != null) {
                    return new CloudFlavourType(cloudFlavour, cloudClient);
                }
            }
        } catch (CloudException ignored) {
        }
        return null;
    }

    public List<List<RoleLifetimeType>> roleLifetimes(@Source List<FlavourType> flavours) {
        return this.flavourRoleLifetimeService.getAllByFlavourIds(flavours.stream().map(FlavourType::getId).toList()).stream()
            .map(flavourRoleLifetimes -> flavourRoleLifetimes.stream()
                .sorted(FlavourRoleLifetime::compareTo)
                .map(RoleLifetimeType::new).toList())
            .toList();
    }

}
