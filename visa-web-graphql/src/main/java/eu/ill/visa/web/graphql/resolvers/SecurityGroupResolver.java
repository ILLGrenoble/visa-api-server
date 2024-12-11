package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.SecurityGroupType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;


@RegisterForReflection
@GraphQLApi
public class SecurityGroupResolver {

    private final CloudClientService cloudClientService;

    @Inject
    public SecurityGroupResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    public List<CloudClientType> cloudClient(@Source List<SecurityGroupType> securityGroups) {
        List<CloudClient> cloudClients = this.cloudClientService.getAll();
        return securityGroups.stream().map(securityGroupType -> {
            return cloudClients.stream().filter(cloudClient -> {
                return cloudClient.getId() == -1 ? securityGroupType.getCloudId() == null : cloudClient.getId().equals(securityGroupType.getCloudId());
            }).findFirst().orElse(null);
        }).map(cloudClient -> cloudClient == null ? null : new CloudClientType(cloudClient)).toList();
    }
}
