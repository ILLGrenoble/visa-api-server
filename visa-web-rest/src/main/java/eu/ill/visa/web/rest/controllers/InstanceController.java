package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceThumbnail;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

import static eu.ill.visa.core.entity.Role.ADMIN_ROLE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/instances")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Authenticated
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
    public Response getThumbnail(@PathParam("instance") final Instance instance) {
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
