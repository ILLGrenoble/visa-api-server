package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.Flavour;

import java.util.Date;
import java.util.Optional;

public record FlavourAvailability(Date date, Flavour flavour, Optional<Long> availableUnits, AvailabilityConfidence confidence) {

    public FlavourAvailability(Flavour flavour, Optional<Long> availableUnits, AvailabilityConfidence confidence) {
        this(new Date(), flavour, availableUnits, confidence);
    }

    public AvailabilityState isAvailable() {
        if (date.after(new Date())) {
            return AvailabilityState.NO;

        } else if (availableUnits.isPresent()) {
            long units = availableUnits.get();
            if (units == 0) {
                return AvailabilityState.NO;

            } else if (confidence.equals(AvailabilityConfidence.CERTAIN)) {
                return  AvailabilityState.YES;

            } else  {
                return  AvailabilityState.MAYBE;
            }

        } else {
            return AvailabilityState.MAYBE;
        }

    }

    public enum AvailabilityConfidence {
        CERTAIN,
        UNCERTAIN,
    }

    public enum AvailabilityState {
        YES,
        NO,
        MAYBE,
    }
}
