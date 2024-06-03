package eu.ill.visa.cloud.providers.openstack.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.providers.openstack.converters.CloudFlavourMixin;
import eu.ill.visa.cloud.providers.openstack.http.responses.FlavorResponse;
import eu.ill.visa.cloud.providers.openstack.http.responses.FlavorsResponse;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

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

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return defaultObjectMapper.copy()
            .addMixIn(CloudFlavour.class, CloudFlavourMixin.class);
    }
}
