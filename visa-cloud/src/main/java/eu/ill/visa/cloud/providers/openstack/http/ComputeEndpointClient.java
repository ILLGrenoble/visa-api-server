package eu.ill.visa.cloud.providers.openstack.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.providers.openstack.converters.CloudFlavourMixin;
import eu.ill.visa.cloud.providers.openstack.http.requests.InstanceActionRequest;
import eu.ill.visa.cloud.providers.openstack.http.requests.ServerRequest;
import eu.ill.visa.cloud.providers.openstack.http.responses.*;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface ComputeEndpointClient {

    String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    @GET
    @Path("/v2/flavors/detail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    FlavorsResponse flavors(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @GET
    @Path("/v2/flavors/{flavourId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    FlavorResponse flavor(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("flavourId") String flavourId);

    @GET
    @Path("/v2/servers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ServersResponse servers(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @POST
    @Path("/v2/servers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ServerCreationResponse createServer(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, final ServerRequest request);

    @GET
    @Path("/v2/servers/{serverId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ServerResponse server(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("serverId") String serverId);

    @DELETE
    @Path("/v2/servers/{serverId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteServer(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("serverId") String serverId);

    @POST
    @Path("/v2/servers/{serverId}/action")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void runServerAction(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("serverId") String serverId, final InstanceActionRequest action);

    @GET
    @Path("/v2/servers/{serverId}/os-security-groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    SecurityGroupsResponse serverSecurityGroups(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("serverId") String serverId);

    @GET
    @Path("/v2/limits")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    LimitsResponse limits(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return defaultObjectMapper.copy()
            .addMixIn(CloudFlavour.class, CloudFlavourMixin.class);
    }

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() == 401) {
            return new CloudAuthenticationException("Authentication failure: " + response.readEntity(String.class) + ")");

        } else if (response.getStatus() == 404) {
            return new CloudNotFoundException("Not found: " + response.readEntity(String.class) + ")");
        }

        return null;
    }

}
