package eu.ill.visa.web.rest.controllers.acm;

import eu.ill.visa.business.services.HealthService;
import eu.ill.visa.core.domain.HealthReport;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.rest.controllers.AbstractController;
import eu.ill.visa.web.rest.module.MetaResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/acm/health")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(Role.APPLICATION_CREDENTIAL_ROLE)
public class HealthController extends AbstractController {

    private final HealthService healthService;

    @Inject
    public HealthController(final HealthService healthService) {
        this.healthService = healthService;
    }

    @GET
    public MetaResponse<HealthReport> health() {
        return createResponse(this.healthService.getHealthReport());
    }

}
