package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.*;
import eu.ill.visa.core.domain.FlavourAvailability;
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

import java.time.Duration;
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
    private final ImageService imageService;
    private final InstanceService instanceService;
    private final FlavourAvailabilityService flavourAvailabilityService;
    final ExperimentService experimentService;

    @Inject
    PlanController(final PlanService planService,
                   final ImageService imageService,
                   final InstanceService instanceService,
                   final FlavourAvailabilityService flavourAvailabilityService,
                   final ExperimentService experimentService) {
        this.planService = planService;
        this.imageService = imageService;
        this.instanceService = instanceService;
        this.flavourAvailabilityService = flavourAvailabilityService;
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

        final List<FlavourAvailability> flavourAvailabilities = this.flavourAvailabilityService.getAllFirstAvailabilities();
        flavourAvailabilities.forEach(flavourAvailability -> {
            logger.info("Flavour {} ({} cpus, {} MB RAM): available = {}, units = {}, confidence = {}", flavourAvailability.flavour().getName(), flavourAvailability.flavour().getCpu(), flavourAvailability.flavour().getMemory(), flavourAvailability.isAvailable(),  flavourAvailability.availableUnits(), flavourAvailability.confidence());
        });

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
            .map(plan -> {
                final FlavourAvailability flavourAvailability = flavourAvailabilities.stream()
                    .filter(availability -> availability.flavour().equals(plan.getFlavour()))
                    .findFirst()
                    .orElse(null);
                return this.toDto(plan, flavourAvailability, this.instanceService.getMaxInstanceDuration(user, plan.getFlavour()));
            })
            .toList();
        return createResponse(planDtos);
    }

    @Path("/{plan}")
    @GET
    public MetaResponse<PlanDto> get(@Context final SecurityContext securityContext, @PathParam("plan") final Plan plan) {
        final User user = this.getUserPrincipal(securityContext);

        return createResponse(this.toDto(plan, this.flavourAvailabilityService.getFirstAvailability(plan.getFlavour()),  this.instanceService.getMaxInstanceDuration(user, plan.getFlavour())));
    }

    private PlanDto toDto(final Plan plan, final FlavourAvailability flavourAvailability, final Duration lifetimeDuration) {
        final Image image = plan.getImage();
        image.setDefaultVdiProtocol(imageService.getDefaultVdiProtocolForImage(image));
        return new PlanDto(plan, flavourAvailability, lifetimeDuration);
    }
}
