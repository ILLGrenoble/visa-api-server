package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.CloudDeviceType;
import eu.ill.visa.web.graphql.types.CloudFlavourType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

@RegisterForReflection
@GraphQLApi
public class CloudFlavourResolver {

    private final CloudClientService cloudClientService;

    public CloudFlavourResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    public List<CloudDeviceType> cloudDevices(@Source CloudFlavourType cloudFlavour) {
        try {
            final CloudClientType cloudClientType = cloudFlavour.getCloudClient();

            CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudClientType.getId());
            if (cloudClient != null) {
                return cloudClient.flavourDevices(cloudFlavour.getId()).stream()
                    .map(CloudDeviceType::new)
                    .toList();
            }
        } catch (CloudException ignored) {
        }
        return null;
    }

}
