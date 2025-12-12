package eu.ill.visa.web.rest.dtos;


import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.domain.FlavourAvailability.AvailabilityConfidence;
import eu.ill.visa.core.domain.FlavourAvailability.AvailabilityData;
import eu.ill.visa.core.entity.Flavour;

import java.util.Date;
import java.util.List;

public class FlavourAvailabilitiesFutureDto {
    private final FlavourDto flavour;
    private final AvailabilityConfidence confidence;
    private final List<FlavourAvailabilityDto> availabilities;

    public FlavourAvailabilitiesFutureDto(final Flavour flavour, final List<FlavourAvailability> flavourAvailabilities) {
        this.flavour = new FlavourDto(flavour);
        if (flavourAvailabilities.isEmpty()) {
            this.confidence = AvailabilityConfidence.UNCERTAIN;
        } else {
            this.confidence = flavourAvailabilities.getFirst().confidence();
        }
        this.availabilities = flavourAvailabilities.stream().map(FlavourAvailabilityDto::new).toList();
    }

    public FlavourDto getFlavour() {
        return flavour;
    }

    public AvailabilityConfidence getConfidence() {
        return confidence;
    }

    public List<FlavourAvailabilityDto> getAvailabilities() {
        return availabilities;
    }

    public static final class FlavourAvailabilityDto {

        private final Date date;
        private final Long availableUnits;
        private final Long totalUnits;

        public FlavourAvailabilityDto(final FlavourAvailability flavourAvailability) {
            this.date = flavourAvailability.date();
            final AvailabilityData availabilityData = flavourAvailability.availability().orElse(null);
            this.availableUnits = availabilityData == null ? null : availabilityData.available();
            this.totalUnits = availabilityData == null ? null : availabilityData.total();
        }

        public Date getDate() {
            return date;
        }

        public Long getAvailableUnits() {
            return availableUnits;
        }

        public Long getTotalUnits() {
            return totalUnits;
        }
    }
}
