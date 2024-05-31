package eu.ill.visa.business.http;


import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.SecurityGroup;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "security-group-service")
public interface SecurityGroupServiceClient {

    @POST
    @Path("/")
    @ClientHeaderParam(name = "x-auth-token", value = "${business.securityGroupServiceClient.authToken}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<SecurityGroup> getSecurityGroups(final Instance instance);

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() != 200) {
            return new RuntimeException("[SecurityGroupServiceClient] Caught HTTP error (" + response.getStatus() + ": " + response.readEntity(String.class) + ") when getting security groups");
        }

        return null;
    }
}
