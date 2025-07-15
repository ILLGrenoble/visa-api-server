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
@RolesAllowed(Role.INSTANCE_CREDENTIAL_ROLE)
public class InstanceController extends AbstractController {

    @GET
    @Path("/{instanceId}")
    public MetaResponse<InstanceDto> getById(@Context final SecurityContext securityContext, @PathParam("instanceId") Long instanceId) {
        final InstanceToken instanceToken = this.getInstanceToken(securityContext);

        if (instanceToken.getInstance() == null) {
            throw new NotFoundException("Instance does not exist");
        }

        if (!instanceToken.getInstance().getId().equals(instanceId)) {
            throw new NotAuthorizedException("Not authorized to retrieve instance");
        }

        return createResponse(this.mapInstance(instanceToken.getInstance()));
    }

    private InstanceDto mapInstance(final Instance instance) {
        // Remove potentially sensitive information
        instance.setMembers(new ArrayList<>());
        return new InstanceDto(instance);
    }
}
