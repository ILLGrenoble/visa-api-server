package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.Image;
import graphql.kickstart.tools.GraphQLResolver;


@Singleton
public class ImageResolver implements GraphQLResolver<Image> {

    private final CloudClient client;

    @Inject
    public ImageResolver(CloudClient client) {
        this.client = client;
    }

    CloudImage cloudImage(Image image) {
        try {
            return client.image(image.getComputeId());
        } catch (CloudException exception) {
            return null;
        }
    }
}
