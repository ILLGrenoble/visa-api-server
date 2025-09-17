package eu.ill.visa.cloud.providers.web.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.providers.web.converters.CloudInstanceMixin;
import eu.ill.visa.cloud.providers.web.converters.CloudLimitMixin;
import eu.ill.visa.cloud.providers.web.http.requests.InstanceSecurityGroupRequest;
import eu.ill.visa.cloud.providers.web.http.responses.ServerCreationResponse;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

public interface WebProviderClient {
    String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    @GET
    @Path("/api/images")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<CloudImage> images(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @GET
    @Path("/api/images/{imageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    CloudImage image(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("imageId") String imageId);

    @GET
    @Path("/api/flavours")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<CloudFlavour> flavours(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @GET
    @Path("/api/flavours/{flavourId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    CloudFlavour flavour(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("flavourId") String flavourId);

    @GET
    @Path("/api/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<CloudDevice> devices(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @GET
    @Path("/api/device/{deviceType}/{deviceIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    CloudDevice device(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("deviceType") CloudDevice.Type deviceType, @PathParam("deviceIdentifier") String deviceIdentifier);

    @GET
    @Path("/api/instances")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<CloudInstance> instances(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @POST
    @Path("/api/instances")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ServerCreationResponse createInstance(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, final CloudInstance cloudInstance);

    @GET
    @Path("/api/instances/{instanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    CloudInstance instance(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId);

    @GET
    @Path("/api/instances/identifiers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<CloudInstanceIdentifier> instanceIdentifiers(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @POST
    @Path("/api/instances/{instanceId}/reboot")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void rebootInstance(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId);

    @POST
    @Path("/api/instances/{instanceId}/shutdown")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void shutdownInstance(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId);

    @POST
    @Path("/api/instances/{instanceId}/start")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void startInstance(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId);

    @DELETE
    @Path("/api/instances/{instanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteInstance(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId);

    @GET
    @Path("/api/instances/{instanceId}/security_groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<String> instanceSecurityGroups(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId);

    @POST
    @Path("/api/instances/{instanceId}/security_groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void addInstanceSecurityGroup(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId, InstanceSecurityGroupRequest securityGroup);

    @POST
    @Path("/api/instances/{instanceId}/security_groups/remove")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void removeInstanceSecurityGroup(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("instanceId") String instanceId, InstanceSecurityGroupRequest securityGroup);

    @GET
    @Path("/api/metrics")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    CloudLimit limits(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @GET
    @Path("/api/security_groups")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<String> securityGroups(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return defaultObjectMapper.copy()
            .addMixIn(CloudInstance.class, CloudInstanceMixin.class)
            .addMixIn(CloudLimit.class, CloudLimitMixin.class);
    }

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
