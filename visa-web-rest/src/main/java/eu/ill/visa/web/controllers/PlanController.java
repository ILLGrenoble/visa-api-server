package eu.ill.visa.web.controllers;

import jakarta.inject.Inject;
import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.dtos.PlanDto;
import io.dropwizard.auth.Auth;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;


@Path("/plans")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class PlanController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    private final PlanService planService;
    private final Mapper mapper;
    final ExperimentService experimentService;

    @Inject
    PlanController(final PlanService planService, final Mapper mapper, final ExperimentService experimentService) {
        this.planService = planService;
        this.mapper = mapper;
        this.experimentService = experimentService;
    }

    @GET
    public Response getAll(@Auth final AccountToken accountToken, @QueryParam("experiments") String experimentIds) {
        final User user = accountToken.getUser();

        List<Plan> plans = null;

        if (user.hasRole(Role.ADMIN_ROLE)) {
            plans = planService.getAllForAdmin();

        } else {
            if (experimentIds != null) {
                List<Experiment> experiments = Stream.of(experimentIds.split(","))
                    .map(experimentId -> {
                        Experiment experiment = this.experimentService.getById(experimentId);
                        if (experiment == null) {
                            logger.warn("Unable to find experiment with id {}", experimentId);
                        }
                        return experiment;
                    })
                    .filter(experiment -> experiment != null)
                    .collect(Collectors.toList());

                plans = planService.getAllForUserAndExperiments(user, experiments);

            } else {
                plans = planService.getAllForUserAndAllInstruments(user);
            }
        }

        plans = plans.stream()
            .sorted((p1, p2) -> {
                Flavour f1 = p1.getFlavour();
                Flavour f2 = p2.getFlavour();
                if (f1.getCpu() < f2.getCpu()) {
                    return -1;
                } else if (f1.getCpu() > f2.getCpu()) {
                    return 1;
                } else return f1.getMemory().compareTo(f2.getMemory());
            })
            .collect(Collectors.toList());
        return createResponse(plans);
    }

    @Path("/{plan}")
    @GET
    public Response get(@PathParam("plan") final Plan plan) {
        return createResponse(mapper.map(plan, PlanDto.class), OK);
    }
}
