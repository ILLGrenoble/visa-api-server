package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.cloud.domain.CloudNetwork;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.ImageNetwork;
import graphql.kickstart.tools.GraphQLResolver;


@Singleton
public class ImageNetworkResolver implements GraphQLResolver<ImageNetwork> {

    private final CloudClient client;

    @Inject
    public ImageNetworkResolver(CloudClient client) {
        this.client = client;
    }

    CloudNetwork cloudNetwork(ImageNetwork network) {
        try {
            return client.network(network.getNetworkId());
        } catch (CloudException exception) {
            return null;
        }
    }
}
