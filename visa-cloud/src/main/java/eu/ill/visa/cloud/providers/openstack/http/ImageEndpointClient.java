package eu.ill.visa.cloud.providers.openstack.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.providers.openstack.converters.CloudImageMixin;
import eu.ill.visa.cloud.providers.openstack.http.responses.ImagesResponse;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface ImageEndpointClient {

    String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    @GET
    @Path("/v2/images")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ImagesResponse images(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @GET
    @Path("/v2/images/{imageId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    CloudImage image(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("imageId") String imageId);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return defaultObjectMapper.copy()
            .addMixIn(CloudImage.class, CloudImageMixin.class);
    }

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() == 401) {
            return new CloudAuthenticationException("Authentication failure: " + response.readEntity(String.class) + ")");
        }

        return null;
    }
}
