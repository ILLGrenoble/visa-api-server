package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.CloudFlavourType;
import eu.ill.visa.web.graphql.types.FlavourType;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class FlavourResolver {

    private final CloudClientService cloudClientService;

    public FlavourResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    public @NotNull CloudClientType cloudClient(@Source FlavourType flavour) {
        CloudClient cloudClient = this.cloudClientService.getCloudClient(flavour.getCloudId());
        if (cloudClient != null) {
            return new CloudClientType(cloudClient);
        }
        return null;
    }

    public CloudFlavourType cloudFlavour(@Source FlavourType flavour) {
        try {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(flavour.getCloudId());
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
