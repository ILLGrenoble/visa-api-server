package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.Flavour;

public record BookingFlavourConfiguration(Flavour flavour, Long maxInstances, Long maxReservationDays, Long maxDaysInAdvance) {
}
