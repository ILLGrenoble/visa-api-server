package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.CloudImageType;
import eu.ill.visa.web.graphql.types.ImageType;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

@GraphQLApi
public class ImageResolver {

    private final CloudClientService cloudClientService;

    public ImageResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }


    public List<CloudClientType> cloudClient(@Source List<ImageType> images) {
        List<CloudClient> cloudClients = this.cloudClientService.getAll();
        return images.stream().map(image -> {
            return cloudClients.stream().filter(cloudClient -> {
                return cloudClient.getId() == -1 ? image.getCloudId() == null : cloudClient.getId().equals(image.getCloudId());
            }).findFirst().orElse(null);
        }).map(cloudClient -> cloudClient == null ? null : new CloudClientType(cloudClient)).toList();
    }

    public CloudImageType cloudImage(@Source ImageType image) {
        try {
            CloudClient cloudClient = this.cloudClientService.getAll().stream().filter(aCloudClient -> {
                return aCloudClient.getId() == -1 ? image.getCloudId() == null : aCloudClient.getId().equals(image.getCloudId());
            }).findFirst().orElse(null);
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
