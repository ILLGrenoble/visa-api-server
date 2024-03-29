package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.Flavour;
import graphql.kickstart.tools.GraphQLResolver;


@Singleton
public class FlavourResolver implements GraphQLResolver<Flavour> {

    private final CloudClientGateway cloudClientGateway;

    @Inject
    public FlavourResolver(final CloudClientGateway cloudClientGateway) {
        this.cloudClientGateway = cloudClientGateway;
    }

    public CloudClient cloudClient(Flavour flavour) {
        return this.cloudClientGateway.getCloudClient(flavour.getCloudId());
    }

    public CloudFlavour cloudFlavour(Flavour flavour) {
        try {
            CloudClient cloudClient = this.cloudClientGateway.getCloudClient(flavour.getCloudId());
            if (cloudClient != null) {
                return cloudClient.flavour(flavour.getComputeId());
            }
        } catch (CloudException ignored) {
        }
        return null;
    }
}
