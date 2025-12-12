package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.Flavour;

public record BookingFlavourConfiguration(Flavour flavour, Long maxInstances, Long maxReservationDays, Long maxDaysInAdvance) {

    public BookingFlavourConfiguration(Flavour flavour, Long maxInstances, Long maxDaysReservation) {
        this(flavour, maxInstances, maxDaysReservation, null);
    }

    public BookingFlavourConfiguration withDefaults(Long maxInstances, Long maxReservationDays, Long maxDaysInAdvance) {
        return new BookingFlavourConfiguration(
            flavour,
            this.maxInstances == null ? maxInstances : this.maxInstances,
            this.maxReservationDays == null ? maxReservationDays : this.maxReservationDays,
            this.maxDaysInAdvance  == null ? maxDaysInAdvance : this.maxDaysInAdvance
        );
    }
}
