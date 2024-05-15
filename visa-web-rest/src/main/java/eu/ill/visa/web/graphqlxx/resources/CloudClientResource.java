package eu.ill.visa.web.graphqlxx.resources;

import com.github.dozermapper.core.Mapper;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.web.graphqlxx.types.CloudClientType;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

//@GraphQLApi
public class CloudClientResource {

    private final CloudClientGateway cloudClientGateway;
    private final Mapper mapper;

    public CloudClientResource(final CloudClientGateway cloudClientGateway,
                               final Mapper mapper) {
        this.cloudClientGateway = cloudClientGateway;
        this.mapper = mapper;
    }

    @Query
    public List<CloudClientType> cloudClients() {
        return this.cloudClientGateway.getAll().stream()
            .map(cloudClient -> mapper.map(cloudClient, CloudClientType.class))
            .toList();
    }
}
