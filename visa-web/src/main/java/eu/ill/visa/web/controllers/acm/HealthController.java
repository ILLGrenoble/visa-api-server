package eu.ill.visa.web.controllers.acm;

import com.google.inject.Inject;
import eu.ill.visa.business.services.HealthService;
import eu.ill.visa.security.tokens.ApplicationToken;
import eu.ill.visa.web.controllers.AbstractController;
import io.dropwizard.auth.Auth;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/acm/health")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class HealthController extends AbstractController {

    private final HealthService healthService;

    @Inject
    public HealthController(final HealthService healthService) {
        this.healthService = healthService;
    }

    @GET
    public Response health(@Auth final ApplicationToken applicationToken) {
        return createResponse(this.healthService.getHealthReport(), OK);
    }

}
