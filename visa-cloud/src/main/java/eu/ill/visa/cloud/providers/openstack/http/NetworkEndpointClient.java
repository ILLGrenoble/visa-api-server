package eu.ill.visa.cloud.providers.openstack.http;


import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.providers.openstack.http.responses.SecurityGroupsResponse;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface NetworkEndpointClient {

    String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    @GET
    @Path("/v2.0/security-groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ClientQueryParam(name = "fields", value = "name")
    SecurityGroupsResponse securityGroups(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() == 401) {
            return new CloudAuthenticationException("Authentication failure: " + response.readEntity(String.class) + ")");

        } else if (response.getStatus() == 404) {
            return new CloudNotFoundException("Not found: " + response.readEntity(String.class) + ")");
        }

        return new CloudClientException("Cloud runtime exception (" + response.getStatus() + "): " + response.readEntity(String.class) + ")");
    }
}
