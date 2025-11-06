package eu.ill.visa.cloud.providers.openstack.http;


import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.providers.openstack.http.responses.ResourceInventoriesResponse;
import eu.ill.visa.cloud.providers.openstack.http.responses.ResourceProvidersResponse;
import eu.ill.visa.cloud.providers.openstack.http.responses.ResourceUsagesResponse;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

public interface PlacementEndpointClient {

    String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    @GET
    @Path("/resource_providers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "OpenStack-API-Version", value = "placement 1.14")
    ResourceProvidersResponse resourceProviders(@HeaderParam(HEADER_X_AUTH_TOKEN) String token);

    @GET
    @Path("/resource_providers/{resource_provider_id}/inventories")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ResourceInventoriesResponse resourceInventories(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("resource_provider_id") String resourceProviderId);

    @GET
    @Path("/resource_providers/{resource_provider_id}/usages")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ResourceUsagesResponse resourceUsages(@HeaderParam(HEADER_X_AUTH_TOKEN) String token, @PathParam("resource_provider_id") String resourceProviderId);

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
