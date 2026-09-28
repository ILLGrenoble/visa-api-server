package eu.ill.visa.web.rest.controllers.acm;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.security.tokens.InstanceToken;
import eu.ill.visa.web.rest.controllers.AbstractController;
import eu.ill.visa.web.rest.dtos.InstanceDto;
import eu.ill.visa.web.rest.module.MetaResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.ArrayList;

@Path("/acm/instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(Role.APPLICATION_CREDENTIAL_ROLE)
public class InstanceController extends AbstractController {

    @GET
    @RolesAllowed({Role.INSTANCE_CREDENTIAL_ROLE,Role.APPLICATION_CREDENTIAL_ROLE})
    @Path("/{instance}")
    public MetaResponse<InstanceDto> get(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        if (!securityContext.isUserInRole(Role.APPLICATION_CREDENTIAL_ROLE)) {
            final InstanceToken instanceToken = this.getInstanceToken(securityContext);

            if (!instanceToken.getInstance().equals(instance)) {
                throw new NotAuthorizedException("Not authorized to retrieve instance");
            }
        }
        return createResponse(this.mapInstance(instance));
    }

    private InstanceDto mapInstance(final Instance instance) {
        // Remove potentially sensitive information
        instance.setMembers(new ArrayList<>());
        return new InstanceDto(instance);
    }
}
