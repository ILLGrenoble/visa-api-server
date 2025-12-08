package eu.ill.visa.web.rest.dtos;


import eu.ill.visa.core.domain.BookingUserConfiguration;

import java.util.List;

public record BookingUserConfigurationDto(boolean enabled, List<BookingFlavourConfigurationDto> flavourConfigurations) {
    public BookingUserConfigurationDto(BookingUserConfiguration configuration) {
        this(
            configuration.enabled(),
            configuration.flavourConfigurations().stream().map(BookingFlavourConfigurationDto::new).toList()
        );
    }
}
