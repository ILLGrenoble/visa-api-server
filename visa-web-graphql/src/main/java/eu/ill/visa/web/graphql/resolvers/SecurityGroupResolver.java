package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.SecurityGroupType;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;


@GraphQLApi
public class SecurityGroupResolver {

    private final CloudClientService cloudClientService;

    @Inject
    public SecurityGroupResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    public @NotNull CloudClientType cloudClient(@Source SecurityGroupType securityGroup) {
        CloudClient cloudClient = this.cloudClientService.getCloudClient(securityGroup.getCloudId());
        if (cloudClient != null) {
            return new CloudClientType(cloudClient);
        }
        return null;
    }
}
