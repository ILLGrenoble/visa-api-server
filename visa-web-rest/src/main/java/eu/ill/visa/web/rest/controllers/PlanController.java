package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.web.rest.dtos.PlanDto;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/plans")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Authenticated
public class PlanController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    private final PlanService planService;
    final ExperimentService experimentService;

    @Inject
    PlanController(final PlanService planService, final ExperimentService experimentService) {
        this.planService = planService;
        this.experimentService = experimentService;
    }

    @GET
    public MetaResponse<List<PlanDto>> getAll(@Context final SecurityContext securityContext, @QueryParam("experiments") String experimentIds) {
        final User user = this.getUserPrincipal(securityContext);

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
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                plans = planService.getAllForUserAndExperiments(user, experiments);

            } else {
                plans = planService.getAllForUserAndAllInstruments(user);
            }
        }

        List<PlanDto> planDtos = plans.stream()
            .sorted((p1, p2) -> {
                Flavour f1 = p1.getFlavour();
                Flavour f2 = p2.getFlavour();
                if (f1.getCpu() < f2.getCpu()) {
                    return -1;
                } else if (f1.getCpu() > f2.getCpu()) {
                    return 1;
                } else return f1.getMemory().compareTo(f2.getMemory());
            })
            .map(PlanDto::new)
            .toList();
        return createResponse(planDtos);
    }

    @Path("/{plan}")
    @GET
    public MetaResponse<PlanDto> get(@PathParam("plan") final Plan plan) {
        return createResponse(new PlanDto(plan));
    }
}
