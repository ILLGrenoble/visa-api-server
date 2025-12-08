package eu.ill.visa.core.domain;


import java.util.List;

public record BookingUserConfiguration(boolean enabled, List<BookingFlavourConfiguration> flavourConfigurations) {
}
