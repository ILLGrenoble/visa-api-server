package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.entity.Plan;

import java.time.Duration;

public class PlanDto {

    private final Long id;
    private final ImageDto image;
    private final FlavourDto flavour;
    private final Boolean preset;

    public PlanDto(final Plan plan) {
        this.id = plan.getId();
        this.image = new ImageDto(plan.getImage());
        this.flavour = new FlavourDto(plan.getFlavour());
        this.preset = plan.getPreset();
    }

    public PlanDto(final Plan plan, final FlavourAvailability flavourAvailability, final Duration lifetimeDuration) {
        this.id = plan.getId();
        this.image = new ImageDto(plan.getImage());
        this.flavour = new FlavourDto(plan.getFlavour(), flavourAvailability, lifetimeDuration);
        this.preset = plan.getPreset();
    }

    public Long getId() {
        return id;
    }

    public ImageDto getImage() {
        return image;
    }

    public FlavourDto getFlavour() {
        return flavour;
    }

    public Boolean getPreset() {
        return preset;
    }
}
