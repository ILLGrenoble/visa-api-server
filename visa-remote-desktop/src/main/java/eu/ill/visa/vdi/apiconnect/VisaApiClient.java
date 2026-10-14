package eu.ill.visa.vdi.apiconnect;

import eu.ill.visa.common.vdi.apiconnect.ClientActivityInput;
import eu.ill.visa.common.vdi.apiconnect.ClientConnectionInput;
import eu.ill.visa.common.vdi.apiconnect.ClientDisconnectionInput;
import eu.ill.visa.common.vdi.apiconnect.VdiConnectionSignature;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@ClientHeaderParam(name = "X-VDI-Auth-Token", value = "${vdi.apiServerAuthToken}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "visa-api-server")
public interface VisaApiClient {

    @GET
    @Path("/authenticate/{protocol}/{token}")
    VdiAuthenticationToken authenticate(final @PathParam("protocol") String protocol, final @PathParam("token") String token);

    @GET
    @Path("/signature/{username}")
    VdiConnectionSignature sign(final @PathParam("username") String username);

    @POST
    @Path("/session/connect")
    void sessionConnect(ClientConnectionInput input);

    @POST
    @Path("/session/disconnect")
    void sessionDisconnect(ClientDisconnectionInput input);

    @POST
    @Path("/session/failure")
    void sessionFailure(ClientConnectionInput input);

    @POST
    @Path("/session/activity")
    void sessionActivity(ClientActivityInput input);

    @POST
    @Path("/session/rtt")
    void sessionRtt(List<VdiRttSample> input);

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        if (response.getStatus() == 401) {
            return new UnauthorizedRuntimeException(String.format("Caught unauthenticated access to VISA: %s", response.readEntity(String.class)));

        } else if (response.getStatus() != 200) {
            return new RuntimeException(String.format("Caught HTTP error (%d: %s) accessing the VISA API", response.getStatus(), response.readEntity(String.class)));
        }

        return null;
    }
}

