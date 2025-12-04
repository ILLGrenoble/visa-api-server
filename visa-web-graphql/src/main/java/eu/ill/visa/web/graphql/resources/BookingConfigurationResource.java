package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.BookingConfiguration;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.BookingConfigurationInput;
import eu.ill.visa.web.graphql.types.BookingConfigurationType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class BookingConfigurationResource {

    private static final Logger logger = LoggerFactory.getLogger(BookingConfigurationResource.class);

    private final BookingConfigurationService bookingConfigurationService;
    private final FlavourService flavourService;
    private final RoleService roleService;
    private final CloudClientService cloudClientService;
    private final CloudProviderService cloudProviderService;

    @Inject
    public BookingConfigurationResource(final BookingConfigurationService bookingConfigurationService,
                                        final FlavourService flavourService,
                                        final RoleService roleService,
                                        final CloudClientService cloudClientService,
                                        final CloudProviderService cloudProviderService) {
        this.bookingConfigurationService = bookingConfigurationService;
        this.flavourService = flavourService;
        this.roleService = roleService;
        this.cloudClientService = cloudClientService;
        this.cloudProviderService = cloudProviderService;
    }

    @Query
    public @NotNull List<BookingConfigurationType> bookingConfigurations() {
        return this.bookingConfigurationService.getAll().stream()
            .map(BookingConfigurationType::new)
            .toList();
    }

    @Query
    public BookingConfigurationType bookingConfigurationForCloudClient(@NotNull @AdaptToScalar(Scalar.Int.class) Long cloudClientId) {
        final BookingConfiguration bookingConfiguration = this.bookingConfigurationService.getByCloudClientId(cloudClientId);
        return bookingConfiguration == null ? null : new BookingConfigurationType(bookingConfiguration);
    }


    @Mutation
    public @NotNull BookingConfigurationType createOrUpdateBookingConfiguration(@NotNull @Valid BookingConfigurationInput input) throws InvalidInputException {
        // Validate the bookingConfiguration input
        this.validateBookingConfigurationInput(input);

        // Check if we already have an existing configuration
        final BookingConfiguration bookingConfiguration = Objects.requireNonNullElse(this.bookingConfigurationService.getByCloudClientId(input.getCloudId()), new BookingConfiguration());

        this.mapToBookingConfiguration(input, bookingConfiguration);
        bookingConfigurationService.save(bookingConfiguration);

        return new BookingConfigurationType(bookingConfiguration);
    }


    private void mapToBookingConfiguration(BookingConfigurationInput input, BookingConfiguration bookingConfiguration) {
        bookingConfiguration.setEnabled(input.getEnabled());
        bookingConfiguration.setMaxInstancesPerReservation(input.getMaxInstancesPerReservation());
        bookingConfiguration.setMaxDaysReservation(input.getMaxDaysReservation());
        bookingConfiguration.setMaxDaysInAdvance(input.getMaxDaysInAdvance());
        bookingConfiguration.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
    }

    private void validateBookingConfigurationInput(BookingConfigurationInput bookingConfigurationInput) throws InvalidInputException {
        // Verify cloud client
        CloudClient cloudClient = this.cloudClientService.getCloudClient(bookingConfigurationInput.getCloudId());
        if (cloudClient == null) {
            throw new InvalidInputException("Invalid cloud Id");
        }


    }

    private CloudProviderConfiguration getCloudProviderConfiguration(Long cloudId) {
        if (cloudId != null && cloudId > 0) {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudId);
            return this.cloudProviderService.getById(cloudClient.getId());
        }
        return null;
    }


}
