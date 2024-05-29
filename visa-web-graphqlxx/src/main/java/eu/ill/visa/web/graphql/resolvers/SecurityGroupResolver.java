package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.SecurityGroupType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;


@GraphQLApi
public class SecurityGroupResolver {

    private final CloudClientGateway cloudClientGateway;

    @Inject
    public SecurityGroupResolver(final CloudClientGateway cloudClientGateway) {
        this.cloudClientGateway = cloudClientGateway;
    }

    public CloudClientType cloudClient(@Source SecurityGroupType securityGroup) {
        CloudClient cloudClient = this.cloudClientGateway.getCloudClient(securityGroup.getCloudId());
        if (cloudClient != null) {
            return new CloudClientType(cloudClient);
        }
        return null;
    }
}
