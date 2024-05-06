package eu.ill.visa.security.suppliers;

import eu.ill.visa.security.tokens.AccountToken;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface AccountsServiceClient {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    AccountToken getAccountToken();

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() == 401) {
            return new RuntimeException("[AccountToken] Caught unauthenticated access to VISA: " + response.readEntity(String.class));

        } else if (response.getStatus() != 200) {
            return new RuntimeException("[AccountToken] Caught HTTP error (" + response.getStatus() + ": " + response.readEntity(String.class) + ") authenticating user access token");
        }

        return null;
    }
}
