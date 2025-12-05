package eu.ill.visa.business.services;


import eu.ill.visa.core.domain.BookingFlavourConfiguration;
import eu.ill.visa.core.entity.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Singleton
public class BookingService {

    private final BookingConfigurationService bookingConfigurationService;
    private final FlavourService flavourService;

    @Inject
    public BookingService(final BookingConfigurationService bookingConfigurationService,
                          final FlavourService flavourService) {
        this.bookingConfigurationService = bookingConfigurationService;
        this.flavourService = flavourService;
    }

    public List<BookingFlavourConfiguration> getBookingFlavourConfigurations(final User user) {
        List<BookingConfiguration> bookingConfigurations = this.bookingConfigurationService.getAll();
        List<BookingFlavourConfiguration> flavourConfigurations = new ArrayList<>();
        for (BookingConfiguration bookingConfiguration : bookingConfigurations) {
            // Check if user has a valid role
            if (bookingConfiguration.getRoles().isEmpty() || user.hasAnyRole(bookingConfiguration.getRoles()) || user.hasRoleWithName(Role.ADMIN_ROLE)) {
                // Find all flavours which the user potentially has access to
                List<Flavour> flavours = bookingConfiguration.getFlavours().isEmpty() ? flavourService.getAllForCloudClient(bookingConfiguration.getCloudId()) : bookingConfiguration.getFlavours();

                // Get the best possible booking configuration for each flavour (remove any flavour that the user can't access)
                flavours.stream()
                    .map(flavour -> this.getBookingFlavourConfiguration(user, flavour, bookingConfiguration))
                    .filter(Objects::nonNull)
                    .forEach(flavourConfigurations::add);
            }
        }

        return flavourConfigurations;
    }

    private BookingFlavourConfiguration getBookingFlavourConfiguration(final User user, final Flavour flavour, BookingConfiguration bookingConfiguration) {
        // Find specific rules applying to this flavour
        List<BookingFlavourRoleConfiguration> flavourRoleConfigurations = bookingConfiguration.getFlavourRoleConfigurations().stream()
            .filter(config -> config.getFlavour().equals(flavour))
            .toList();

        // See if specific rules apply to the flavour
        if (flavourRoleConfigurations.isEmpty()) {
            // Apply the general configuration to the flavour
            return new BookingFlavourConfiguration(flavour, bookingConfiguration.getMaxInstancesPerReservation(), bookingConfiguration.getMaxDaysReservation(), bookingConfiguration.getMaxDaysInAdvance());

        } else {
            // Only use the specific rules: if User Role is not included then they can't reserve this flavour
            return this.flavourConfigurationFromSpecificRules(user, flavour, bookingConfiguration.getFlavourRoleConfigurations(), bookingConfiguration.getMaxDaysInAdvance());
        }
    }

    private BookingFlavourConfiguration flavourConfigurationFromSpecificRules(final User user, final Flavour flavour, List<BookingFlavourRoleConfiguration> flavourRoleConfigurations, Long maxDaysInAdvance) {
        // Ensure that the user has the right Roles applying to the flavour rules and find the best conditions for them
        return flavourRoleConfigurations.stream()
            .filter(config -> config.getRole() == null || user.hasRole(config.getRole()) || user.hasRoleWithName(Role.ADMIN_ROLE))
            .map(config -> new BookingFlavourConfiguration(flavour, config.getMaxInstancesPerReservation(), config.getMaxDaysReservation(), maxDaysInAdvance))
            .reduce(null, (acc, next) -> {
                if (acc == null) {
                    return next;
                } else {
                    Long maxInstances = Math.max(next.maxInstances(),  acc.maxInstances());
                    Long maxReservationDays = Math.max(next.maxReservationDays(), acc.maxReservationDays());
                    return new BookingFlavourConfiguration(flavour, maxInstances, maxReservationDays, maxDaysInAdvance);
                }
            });
    }
}
