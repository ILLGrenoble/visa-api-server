package eu.ill.visa.cloud.providers.openstack.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.ill.visa.cloud.exceptions.CloudRuntimeException;
import eu.ill.visa.cloud.providers.openstack.http.requests.AuthenticationRequest;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface IdentityEndpointClient {

    @POST
    @Path("/v3/auth/tokens")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response authenticate(final AuthenticationRequest authenticationRequest);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return defaultObjectMapper.copy()
            .enable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() != 200) {
            return new CloudRuntimeException("Error authenticating OpenStack (" + response.getStatus() + ": " + response.readEntity(String.class) + ")");
        }

        return null;
    }

}
