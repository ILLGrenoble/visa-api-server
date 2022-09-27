package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.Image;
import graphql.kickstart.tools.GraphQLResolver;


@Singleton
public class ImageResolver implements GraphQLResolver<Image> {

    private final CloudClientGateway cloudClientGateway;

    @Inject
    public ImageResolver(final CloudClientGateway cloudClientGateway) {
        this.cloudClientGateway = cloudClientGateway;
    }

    public CloudClient cloudClient(Image image) {
        return this.cloudClientGateway.getCloudClient(image.getCloudId());
    }

    public CloudImage cloudImage(Image image) {
        try {
            CloudClient cloudClient = this.cloudClientGateway.getCloudClient(image.getCloudId());
            if (cloudClient != null) {
                return cloudClient.image(image.getComputeId());
            }
        } catch (CloudException ignored) {
        }
        return null;
    }
}
