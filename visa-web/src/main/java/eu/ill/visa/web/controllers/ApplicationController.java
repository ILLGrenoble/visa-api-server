package eu.ill.visa.web.controllers;

import com.google.inject.Inject;
import eu.ill.visa.security.tokens.ApplicationToken;
import io.dropwizard.auth.Auth;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/app")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class ApplicationController extends AbstractController {

    @Inject
    public ApplicationController() {
    }

    @GET
    @Path("/health")
    public Response health(@Auth final ApplicationToken applicationToken) {
        return createResponse(applicationToken.getName(), OK);
    }

}
