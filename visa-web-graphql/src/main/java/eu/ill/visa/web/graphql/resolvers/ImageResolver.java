package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.CloudImageType;
import eu.ill.visa.web.graphql.types.ImageType;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class ImageResolver {

    private final CloudClientService cloudClientService;

    public ImageResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }


    public @NotNull CloudClientType cloudClient(@Source ImageType image) {
        CloudClient cloudClient = this.cloudClientService.getCloudClient(image.getCloudId());
        if (cloudClient != null) {
            return new CloudClientType(cloudClient);
        }
        return null;
    }

    public CloudImageType cloudImage(@Source ImageType image) {
        try {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(image.getCloudId());
            if (cloudClient != null) {
                CloudImage cloudImage = cloudClient.image(image.getComputeId());
                if (cloudImage != null) {
                    return new CloudImageType(cloudImage);
                }
            }
        } catch (CloudException ignored) {
        }
        return null;
    }
}
