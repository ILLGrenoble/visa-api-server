package eu.ill.visa.web.graphql.queries.resolvers.fields;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.Flavour;
import graphql.kickstart.tools.GraphQLResolver;


@ApplicationScoped
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
