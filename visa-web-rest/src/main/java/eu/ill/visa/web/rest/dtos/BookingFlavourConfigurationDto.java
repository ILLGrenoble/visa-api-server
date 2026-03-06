package eu.ill.visa.web.rest.dtos;


import eu.ill.visa.core.domain.BookingFlavourConfiguration;

public record BookingFlavourConfigurationDto(FlavourDto flavour, Long maxInstances, Long maxReservationDays) {
    public BookingFlavourConfigurationDto(BookingFlavourConfiguration configuration) {
        this(
            new FlavourDto(configuration.flavour()),
            configuration.maxInstances(),
            configuration.maxReservationDays()
        );
    }
}
