package eu.ill.visa.web.rest.dtos;


import eu.ill.visa.core.domain.BookingFlavourConfiguration;

public record BookingFlavourConfigurationDto(FlavourDto flavour, boolean autoAccept, Long maxInstances, Long maxReservationDays) {
    public BookingFlavourConfigurationDto(BookingFlavourConfiguration configuration) {
        this(
            new FlavourDto(configuration.flavour()),
            configuration.autoAccept(),
            configuration.maxInstances(),
            configuration.maxReservationDays()
        );
    }
}
