package eu.ill.visa.business.services;


import eu.ill.visa.core.domain.BookingFlavourConfiguration;
import eu.ill.visa.core.domain.BookingUserConfiguration;
import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.entity.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

@Singleton
@Transactional
public class BookingService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingConfigurationService bookingConfigurationService;
    private final BookingRequestService bookingRequestService;
    private final FlavourAvailabilityService flavourAvailabilityService;
    private final FlavourService flavourService;

    @Inject
    public BookingService(final BookingConfigurationService bookingConfigurationService,
                          final BookingRequestService bookingRequestService,
                          final FlavourAvailabilityService flavourAvailabilityService,
                          final FlavourService flavourService) {
        this.bookingConfigurationService = bookingConfigurationService;
        this.bookingRequestService = bookingRequestService;
        this.flavourAvailabilityService = flavourAvailabilityService;
        this.flavourService = flavourService;
    }

    public BookingUserConfiguration getBookingUserConfiguration(final User user) {
        List<BookingConfiguration> bookingConfigurations = this.bookingConfigurationService.getAll().stream()
            .filter(BookingConfiguration::isEnabled)
            .toList();

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

        boolean enabled = !flavourConfigurations.isEmpty();

        return new BookingUserConfiguration(enabled, flavourConfigurations);
    }

    public BookingRequestValidation validateAndCreateBookingRequest(final BookingRequest bookingRequest) {
        List<String> errors = new ArrayList<>();
        // Verify user access to flavours in the request
        final BookingUserConfiguration bookingUserConfiguration = this.getBookingUserConfiguration(bookingRequest.getOwner());

        // Verify dates of request
        final LocalDate startDate = bookingRequest.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate endDate = bookingRequest.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (!startDate.isAfter(LocalDate.now())) {
            errors.add(format("The reservation start date (%s) is too early", startDate));
        }
        long daysToReservation = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        long reservationDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        final List<Flavour> flavours = bookingRequest.getFlavours().stream().map(BookingRequestFlavour::getFlavour).toList();
        final Map<Flavour, List<FlavourAvailability>> flavoursAvailabilities = this.flavourAvailabilityService.getFutureAvailabilities(flavours, startDate, endDate);

        // Verify flavour
        for (BookingRequestFlavour requestFlavour : bookingRequest.getFlavours()) {
            final Flavour requestedFlavour = requestFlavour.getFlavour();
            final Long requestedQuantity = requestFlavour.getQuantity();

            // Ensure flavour is available to user
            final BookingFlavourConfiguration flavourConfiguration = bookingUserConfiguration.flavourConfigurations().stream()
                .filter(configuration -> configuration.flavour().equals(requestedFlavour))
                .findFirst().orElse(null);

            boolean flavourOk = true;

            if (flavourConfiguration == null) {
                flavourOk = false;
                logger.warn("User ({}) has requested a flavour ({}) that is not available to them for reservation", bookingRequest.getOwner().getFullNameAndId(), requestedFlavour.getName());

            } else {
                final Long maxDaysInAdvance = flavourConfiguration.maxDaysInAdvance();
                final Long maxReservationDays = flavourConfiguration.maxReservationDays();
                final Long maxInstances =  flavourConfiguration.maxInstances();

                // Ensure days in advance is valid
                if (maxDaysInAdvance != null && daysToReservation > maxDaysInAdvance) {
                    flavourOk = false;
                    logger.warn("User ({}) has requested a flavour ({}) after the max allowed days in the future ({} > {})", bookingRequest.getOwner().getFullNameAndId(), requestedFlavour.getName(), daysToReservation, flavourConfiguration.maxDaysInAdvance());
                }

                // Ensure days reservation is valid
                if (maxReservationDays != null && reservationDays > maxReservationDays) {
                    flavourOk = false;
                    logger.warn("User ({}) has requested a flavour ({}) for an unacceptable duration ({} > {})", bookingRequest.getOwner().getFullNameAndId(), requestedFlavour.getName(), reservationDays, flavourConfiguration.maxReservationDays());
                }

                // Ensure quantity of instances is valid
                if (maxInstances != null && requestFlavour.getQuantity() > maxInstances) {
                    flavourOk = false;
                    logger.warn("User ({}) has requested too many instances of a flavour ({}) ({} > {})", bookingRequest.getOwner().getFullNameAndId(), requestedFlavour.getName(), requestFlavour.getQuantity(), flavourConfiguration.maxInstances());
                }

                // Ensure availability of flavour for the dates
                final List<FlavourAvailability> flavourAvailabilities = flavoursAvailabilities.get(requestedFlavour);
                if (flavourAvailabilities == null) {
                    flavourOk = false;
                    logger.warn("Unable to obtain flavour availabilities for flavour \"{}\"",  requestedFlavour.getName());

                } else {
                    // determine if any of the availabilities returned have insufficient available quantities
                    boolean availabilityOk = flavourAvailabilities.stream()
                        .noneMatch(aFlavourAvailability -> {
                            final FlavourAvailability.AvailabilityData availability = aFlavourAvailability.availability().orElse(null);
                            return availability == null || availability.available() < requestedQuantity;
                        });

                    if (!availabilityOk) {
                        flavourOk = false;
                        logger.info("The flavour \"{}\" is unavailable in sufficient quantities (or cannot be determined) during the reservation dates ({} to {})",  requestedFlavour.getName(), startDate, endDate);
                    }
                }

                if (!flavourOk) {
                    errors.add(format("The requested flavour (%s) is not available for the reservation request", requestedFlavour.getName()));
                }
            }
        }

        boolean isValid = errors.isEmpty();
        if (isValid) {
            this.bookingRequestService.create(bookingRequest);
            logger.info("Booking request has been successfully validated and created: {}", bookingRequest);
        }

        return new BookingRequestValidation(bookingRequest, isValid, errors);
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
            BookingFlavourConfiguration specificConfiguration = this.flavourConfigurationFromSpecificRules(user, flavour, bookingConfiguration.getFlavourRoleConfigurations());
            return specificConfiguration.withDefaults(bookingConfiguration.getMaxInstancesPerReservation(), bookingConfiguration.getMaxDaysReservation(), bookingConfiguration.getMaxDaysInAdvance());
        }
    }

    private BookingFlavourConfiguration flavourConfigurationFromSpecificRules(final User user, final Flavour flavour, List<BookingFlavourRoleConfiguration> flavourRoleConfigurations) {
        // Ensure that the user has the right Roles applying to the flavour rules and find the best conditions for them
        return flavourRoleConfigurations.stream()
            .filter(config -> config.getFlavour().equals(flavour))
            .filter(config -> config.getRole() == null || user.hasRole(config.getRole()) || user.hasRoleWithName(Role.ADMIN_ROLE))
            .map(config -> {
                return new BookingFlavourConfiguration(flavour, config.getMaxInstancesPerReservation(), config.getMaxDaysReservation());
            })
            .reduce(null, (acc, next) -> {
                if (acc == null) {
                    return next;
                } else {
                    Long maxInstances = acc.maxInstances() == null ? next.maxInstances() : next.maxInstances() == null ? acc.maxInstances() : Math.max(next.maxInstances(),  acc.maxInstances());
                    Long maxReservationDays = acc.maxReservationDays() == null ? next.maxReservationDays() : next.maxReservationDays() == null ? acc.maxReservationDays() : Math.max(next.maxReservationDays(),  acc.maxReservationDays());
                    return new BookingFlavourConfiguration(flavour, maxInstances, maxReservationDays);
                }
            });
    }

    public record BookingRequestValidation(BookingRequest bookingRequest, boolean isValid, List<String> errors) {}
}
