package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.domain.FlavourAvailability.AvailabilityConfidence;
import eu.ill.visa.core.domain.FlavourAvailability.AvailabilityData;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("FlavourAvailability")
public class FlavourAvailabilityType {

    private final @NotNull Date date;
    private final @NotNull AvailabilityConfidence confidence;
    @AdaptToScalar(Scalar.Int.class)
    private final Long availableUnits;
    private final Long totalUnits;

    public FlavourAvailabilityType(final FlavourAvailability flavourAvailability) {
        this.date = flavourAvailability.date();
        this.confidence = flavourAvailability.confidence();
        final AvailabilityData availabilityData = flavourAvailability.availability().orElse(null);
        this.availableUnits = availabilityData == null ? null : availabilityData.available();
        this.totalUnits = availabilityData == null ? null : availabilityData.total();
    }

    public Date getDate() {
        return date;
    }

    public AvailabilityConfidence getConfidence() {
        return confidence;
    }

    public Long getAvailableUnits() {
        return availableUnits;
    }

    public Long getTotalUnits() {
        return totalUnits;
    }
}
