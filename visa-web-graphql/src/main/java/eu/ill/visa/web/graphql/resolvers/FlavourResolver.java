package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.CloudFlavourType;
import eu.ill.visa.web.graphql.types.FlavourType;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

@GraphQLApi
public class FlavourResolver {

    private final CloudClientService cloudClientService;

    public FlavourResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
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
                    return new CloudFlavourType(cloudFlavour);
                }
            }
        } catch (CloudException ignored) {
        }
        return null;
    }

}
