package eu.ill.visa.web.graphql.queries.resolvers.fields;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.Image;
import graphql.kickstart.tools.GraphQLResolver;


@ApplicationScoped
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
