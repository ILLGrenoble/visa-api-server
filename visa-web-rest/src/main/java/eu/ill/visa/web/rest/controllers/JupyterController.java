package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.InstanceExpirationService;
import eu.ill.visa.business.services.InstanceJupyterSessionService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.web.rest.dtos.InstanceDto;
import eu.ill.visa.web.rest.dtos.JupyterNotebookSessionInput;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/jupyter/instances")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class JupyterController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(JupyterController.class);

    private final InstanceService instanceService;
    private final InstanceJupyterSessionService instanceJupyterSessionService;
    private final InstanceExpirationService instanceExpirationService;

    @Inject
    public JupyterController(final InstanceService instanceService,
                             final InstanceJupyterSessionService instanceJupyterSessionService,
                             final InstanceExpirationService instanceExpirationService) {
        this.instanceService = instanceService;
        this.instanceJupyterSessionService = instanceJupyterSessionService;
        this.instanceExpirationService = instanceExpirationService;
    }

    @GET
    @Path("/{instance}")
    @Authenticated
    public MetaResponse<InstanceDto> get(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);

        if (this.isAuthorisedForJupyter(user, instance)) {
            InstanceDto instanceDto = new InstanceDto(instance);

            // Update last seen at
            instance.updateLastSeenAt();
            this.instanceService.save(instance);

            // Remove from expirations
            this.instanceExpirationService.onInstanceActivated(instance);

            return createResponse(instanceDto);
        }

        throw new NotFoundException();
    }

    @POST
    @Path("/{instance}/notebook/open")
    @Authenticated
    public void jupyterSessionOpen(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance, @NotNull @Valid final JupyterNotebookSessionInput input) {
        final User user = this.getUserPrincipal(securityContext);

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            this.instanceJupyterSessionService.create(instance, user, input.getKernelId(), input.getSessionId());
        }

        throw new NotFoundException();
    }

    @POST
    @Path("/{instance}/notebook/close")
    public void jupyterSessionClose(@PathParam("instance") Instance instance, @NotNull @Valid final JupyterNotebookSessionInput input) {
        // Allow any access this endpoint: allows the visa-jupyter-proxy to remove zombie sessions without authentication (needs to know both kernelId and sessionId so security risk is low)
        this.instanceJupyterSessionService.destroy(instance, input.getKernelId(), input.getSessionId());
    }

    private boolean isAuthorisedForJupyter(User user, Instance instance) {
        final InstanceMember member = instance.getMember(user);
        if (member == null) {
            return false;
        }

        return member.isRole(InstanceMemberRole.OWNER);
    }

}
