package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.business.services.ImageService;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.Image;
import eu.ill.visa.core.entity.Plan;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.inputs.PlanInput;
import eu.ill.visa.web.graphql.types.PlanType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class PlanResource {

    private final PlanService planService;
    private final FlavourService flavourService;
    private final ImageService imageService;

    @Inject
    public PlanResource(final PlanService planService,
                        final FlavourService flavourService,
                        final ImageService imageService) {
        this.planService = planService;
        this.flavourService = flavourService;
        this.imageService = imageService;
    }

    /**
     * Get a list of plans
     *
     * @return a list of plans
     */
    @Query
    public @NotNull List<PlanType> plans() {
        return this.planService.getAllForAdmin().stream()
            .map(PlanType::new)
            .toList();
    }


    /**
     * Create a new plan
     *
     * @param input the plan properties
     * @return the newly created plan
     * @throws EntityNotFoundException thrown if the given flavour is not found
     * @throws EntityNotFoundException thrown if the given image is not found
     */
    @Mutation
    public @NotNull PlanType createPlan(@NotNull @Valid PlanInput input) throws EntityNotFoundException {
        final Flavour flavour = this.flavourService.getById(input.getFlavourId());
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }
        final Image image = this.imageService.getById(input.getImageId());
        if (image == null) {
            throw new EntityNotFoundException("Image not found for the given id");
        }
        if (input.getPreset()) {
            // reset all plans to preset of false
            planService.getAllForAdmin().forEach(object -> {
                object.setPreset(false);
                planService.save(object);
            });
        }
        final Plan plan = new Plan();
        plan.setFlavour(flavour);
        plan.setImage(image);
        plan.setPreset(input.getPreset());
        planService.create(plan);
        return new PlanType(plan);
    }

    /**
     * Update a plan
     *
     * @param id    the plan id
     * @param input the plan properties
     * @return the updated plan
     * @throws EntityNotFoundException thrown if the given flavour is not found
     * @throws EntityNotFoundException thrown if the given image is not found
     * @throws EntityNotFoundException thrown if the given plan is not found
     */
    @Mutation
    public @NotNull PlanType updatePlan(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull @Valid PlanInput input) throws EntityNotFoundException {
        final Flavour flavour = this.flavourService.getById(input.getFlavourId());
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }
        final Image image = this.imageService.getById(input.getImageId());
        if (image == null) {
            throw new EntityNotFoundException("Image not found for the given id");
        }
        final Plan plan = planService.getById(id);
        if (plan == null) {
            throw new EntityNotFoundException("Plan not found for the given id");
        }

        if (input.getPreset()) {
            // reset all plans to preset of false
            planService.getAllForAdmin().forEach(object -> {
                object.setPreset(false);
                planService.save(object);
            });
        }
        plan.setFlavour(flavour);
        plan.setImage(image);
        plan.setPreset(input.getPreset());
        planService.save(plan);
        return new PlanType(plan);
    }

    /**
     * Delete a plan for a given id
     *
     * @param id the plan id
     * @return the deleted plan
     * @throws EntityNotFoundException thrown if the plan is not found
     * @throws EntityNotFoundException thrown if there are instances associated to the plan
     */
    @Mutation
    public @NotNull PlanType deletePlan(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final Plan plan = planService.getById(id);
        if (plan == null) {
            throw new EntityNotFoundException("Plan not found for the given id");
        }
        plan.setDeleted(true);
        planService.save(plan);
        return new PlanType(plan);
    }


}
