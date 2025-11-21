package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.domain.FlavourAvailability.AvailabilityConfidence;
import eu.ill.visa.core.entity.Flavour;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

@Type("FlavourAvailabilitiesFuture")
public class FlavourAvailabilitiesFutureType {

    private final @NotNull FlavourType flavour;
    private final @NotNull AvailabilityConfidence confidence;
    private final @NotNull List<@NotNull FlavourAvailabilityType> availabilities;

    public FlavourAvailabilitiesFutureType(final Flavour flavour, final List<FlavourAvailability> flavourAvailabilities) {
        this.flavour = new FlavourType(flavour);
        if (flavourAvailabilities.isEmpty()) {
            this.confidence = AvailabilityConfidence.UNCERTAIN;
        } else {
            this.confidence = flavourAvailabilities.getFirst().confidence();
        }
        this.availabilities = flavourAvailabilities.stream().map(FlavourAvailabilityType::new).toList();
    }

    public FlavourType getFlavour() {
        return flavour;
    }

    public AvailabilityConfidence getConfidence() {
        return confidence;
    }

    public List<FlavourAvailabilityType> getAvailabilities() {
        return availabilities;
    }
}
