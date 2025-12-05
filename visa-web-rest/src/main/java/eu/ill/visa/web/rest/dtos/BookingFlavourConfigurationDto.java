package eu.ill.visa.web.rest.dtos;


import eu.ill.visa.core.domain.BookingFlavourConfiguration;

public record BookingFlavourConfigurationDto(FlavourDto flavour, Long maxInstances, Long maxReservationDays, Long maxDaysInAdvance) {
    public BookingFlavourConfigurationDto(BookingFlavourConfiguration configuration) {
        this(
            new FlavourDto(configuration.flavour()),
            configuration.maxInstances(),
            configuration.maxReservationDays(),
            configuration.maxDaysInAdvance()
        );
    }
}
