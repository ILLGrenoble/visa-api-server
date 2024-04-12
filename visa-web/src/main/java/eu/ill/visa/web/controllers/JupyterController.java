package eu.ill.visa.web.controllers;

import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceExpirationService;
import eu.ill.visa.business.services.InstanceJupyterSessionService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceMember;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.core.domain.enumerations.InstanceMemberRole;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.dtos.InstanceDto;
import eu.ill.visa.web.dtos.JupyterNotebookSessionDto;
import io.dropwizard.auth.Auth;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/jupyter/instances")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class JupyterController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(JupyterController.class);

    private final InstanceService instanceService;
    private final InstanceJupyterSessionService instanceJupyterSessionService;
    private final InstanceExpirationService instanceExpirationService;
    private final Mapper mapper;

    @Inject
    public JupyterController(final InstanceService instanceService,
                             final InstanceJupyterSessionService instanceJupyterSessionService,
                             final InstanceExpirationService instanceExpirationService,
                             final Mapper mapper) {
        this.instanceService = instanceService;
        this.instanceJupyterSessionService = instanceJupyterSessionService;
        this.instanceExpirationService = instanceExpirationService;
        this.mapper = mapper;
    }

    @GET
    @Path("/{instance}")
    @PermitAll
    public Response get(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();

        if (this.isAuthorisedForJupyter(user, instance)) {
            InstanceDto instanceDto = mapper.map(instance, InstanceDto.class);

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
    @PermitAll
    public Response jupyterSessionOpen(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance, @NotNull @Valid final JupyterNotebookSessionDto jupyterNotebookSessionDto) {
        final User user = accountToken.getUser();

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            this.instanceJupyterSessionService.create(instance, user, jupyterNotebookSessionDto.getKernelId(), jupyterNotebookSessionDto.getSessionId());

            return createResponse();
        }

        throw new NotFoundException();
    }

    @POST
    @Path("/{instance}/notebook/close")
    public Response jupyterSessionClose(@PathParam("instance") Instance instance, @NotNull @Valid final JupyterNotebookSessionDto jupyterNotebookSessionDto) {
        // Allow any access this endpoint: allows the visa-jupyter-proxy to remove zombie sessions without authentication (needs to know both kernelId and sessionId so security risk is low)
        this.instanceJupyterSessionService.destroy(instance, jupyterNotebookSessionDto.getKernelId(), jupyterNotebookSessionDto.getSessionId());

        return createResponse();
    }

    private boolean isAuthorisedForJupyter(User user, Instance instance) {
        final InstanceMember member = instance.getMember(user);
        if (member == null) {
            return false;
        }

        return member.isRole(InstanceMemberRole.OWNER);
    }

}
