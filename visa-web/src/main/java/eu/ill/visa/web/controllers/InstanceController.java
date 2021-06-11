package eu.ill.visa.web.controllers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceThumbnail;
import eu.ill.visa.security.tokens.AccountToken;
import io.dropwizard.auth.Auth;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static eu.ill.visa.core.domain.Role.ADMIN_ROLE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/instances")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class InstanceController {

    private final InstanceService instanceService;

    @Inject
    public InstanceController(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @GET
    @Path("/{instance}/thumbnail")
    @Produces("image/jpeg")
    @RolesAllowed({ADMIN_ROLE})
    public Response getThumbnail(@Auth final AccountToken accountToken,
                                 @PathParam("instance") final Instance instance) {
        final InstanceThumbnail thumbnail = instanceService.getThumbnailForInstance(instance);
        if (thumbnail != null) {
            final Response.ResponseBuilder response = Response.ok(thumbnail.getData());
            return response.build();
        }
        // return fallback image...
        final InputStream data = getClass().getResourceAsStream("/images/thumbnail.jpg");
        final Response.ResponseBuilder response = Response.ok(data);
        return response.build();
    }
}
