package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.Flavour;

public record BookingFlavourConfiguration(Flavour flavour, boolean autoAccept, Long maxInstances, Long maxReservationDays) {

    public BookingFlavourConfiguration withDefaults(Long maxInstances, Long maxReservationDays) {
        return new BookingFlavourConfiguration(
            flavour,
            this.autoAccept,
            this.maxInstances == null ? maxInstances : this.maxInstances,
            this.maxReservationDays == null ? maxReservationDays : this.maxReservationDays
        );
    }
}
