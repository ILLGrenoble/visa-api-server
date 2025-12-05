package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.BookingConfigurationInput;
import eu.ill.visa.web.graphql.inputs.BookingConfigurationInput.BookingFlavourRoleConfigurationInput;
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

import static java.util.Objects.requireNonNullElse;

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
        final BookingConfiguration bookingConfiguration = requireNonNullElse(this.bookingConfigurationService.getByCloudClientId(input.getCloudId()), new BookingConfiguration());

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

        // Update roles
        bookingConfiguration.getRoles().removeIf(role -> !input.getRoleIds().contains(role.getId()));
        final List<Long> roleIds = bookingConfiguration.getRoles().stream().map(Role::getId).toList();
        input.getRoleIds().forEach(roleId -> {
            if (!roleIds.contains(roleId)) {
                bookingConfiguration.getRoles().add(roleService.getById(roleId));
            }
        });

        // Update flavours
        bookingConfiguration.getFlavours().removeIf(flavour -> !input.getFlavourIds().contains(flavour.getId()));
        final List<Long> flavourIds = bookingConfiguration.getFlavours().stream().map(Flavour::getId).toList();
        input.getFlavourIds().forEach(flavourId -> {
            if (!flavourIds.contains(flavourId)) {
                bookingConfiguration.getFlavours().add(flavourService.getById(flavourId));
            }
        });

        // Sort out the role-flavour configurations: ones that don't exist any more
        final List<BookingFlavourRoleConfiguration> flavourRoleConfigurations = bookingConfiguration.getFlavourRoleConfigurations();

        flavourRoleConfigurations.removeIf(flavourRoleConfiguration -> {
            final BookingFlavourRoleConfigurationInput flavourRoleConfigurationInput = input.getFlavourRoleConfigurations().stream()
                .filter(bookingFlavourRoleConfigurationInput -> {
                    boolean flavourOk = bookingFlavourRoleConfigurationInput.getFlavourId().equals(flavourRoleConfiguration.getFlavour().getId());
                    Long inputRoleId = requireNonNullElse(bookingFlavourRoleConfigurationInput.getRoleId(), -1L);
                    Long roleId = flavourRoleConfiguration.getRole() == null ? -1L : flavourRoleConfiguration.getRole().getId();
                    return flavourOk && roleId.equals(inputRoleId);
                })
                .findFirst().orElse(null);

            return flavourRoleConfigurationInput == null;
        });

        // Sort out the role-flavour configurations: new or modified ones
        for (BookingFlavourRoleConfigurationInput flavourRoleConfigurationInput : input.getFlavourRoleConfigurations()) {
            final Long flavourInputId = flavourRoleConfigurationInput.getFlavourId();
            final Long roleInputId = requireNonNullElse(flavourRoleConfigurationInput.getRoleId(), -1L);

            final BookingFlavourRoleConfiguration flavourRoleConfiguration = flavourRoleConfigurations.stream()
                .filter(bookingFlavourRoleConfiguration -> {
                    final Long flavourId = bookingFlavourRoleConfiguration.getFlavour().getId();
                    final Long roleId = bookingFlavourRoleConfiguration.getRole() == null ? -1L : bookingFlavourRoleConfiguration.getRole().getId();

                    return flavourId.equals(flavourInputId) && roleId.equals(roleInputId);
                })
                .findFirst().orElse(null);

            if (flavourRoleConfiguration == null) {
                flavourRoleConfigurations.add(BookingFlavourRoleConfiguration.Builder()
                    .flavour(this.flavourService.getById(flavourInputId))
                    .role(roleInputId == -1 ? null : this.roleService.getById(roleInputId))
                    .maxDaysReservation(flavourRoleConfigurationInput.getMaxDaysReservation())
                    .maxInstancesPerReservation(flavourRoleConfigurationInput.getMaxInstancesPerReservation())
                    .build());
            } else {
                flavourRoleConfiguration.setMaxDaysReservation(flavourRoleConfigurationInput.getMaxDaysReservation());
                flavourRoleConfiguration.setMaxInstancesPerReservation(flavourRoleConfigurationInput.getMaxInstancesPerReservation());
            }
        }
    }

    private void validateBookingConfigurationInput(BookingConfigurationInput bookingConfigurationInput) throws InvalidInputException {
        // Verify cloud client
        CloudClient cloudClient = this.cloudClientService.getCloudClient(bookingConfigurationInput.getCloudId());
        if (cloudClient == null) {
            throw new InvalidInputException("Invalid cloud Id");
        }
        final Long cloudId = bookingConfigurationInput.getCloudId() == null ? -1L : bookingConfigurationInput.getCloudId();

        // Check the roles and flavours
        final List<Long> roleIds = this.roleService.getAllRolesAndGroups().stream().map(Role::getId).toList();
        final List<Long> flavourIds = this.flavourService.getAllForAdmin().stream().filter(flavour -> {
            Long flavourCloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();
            return flavourCloudId.equals(cloudId);
        }).map(Flavour::getId).toList();

        // Roles Ids
        for (Long roleId : bookingConfigurationInput.getRoleIds()) {
            if (!roleIds.contains(roleId)) {
                throw new InvalidInputException("Invalid role Id");
            }
        }

        // Flavour Ids
        for (Long flavourId : bookingConfigurationInput.getFlavourIds()) {
            if (!flavourIds.contains(flavourId)) {
                throw new InvalidInputException("Invalid flavour Id");
            }
        }

        // Check flavourRole configurations
        final List<Long> roleIdsForFlavourRoleConfigurations = bookingConfigurationInput.getRoleIds().isEmpty() ? roleIds : bookingConfigurationInput.getRoleIds();
        final List<Long> flavourIdsForFlavourRoleConfiguration = bookingConfigurationInput.getFlavourIds().isEmpty() ? flavourIds : bookingConfigurationInput.getFlavourIds();
        for (final BookingFlavourRoleConfigurationInput flavourRoleConfigurationInput : bookingConfigurationInput.getFlavourRoleConfigurations()) {
            final Long roleId = flavourRoleConfigurationInput.getRoleId();
            final Long flavourId = flavourRoleConfigurationInput.getFlavourId();
            if (!flavourIdsForFlavourRoleConfiguration.contains(flavourId)) {
                throw new InvalidInputException("Invalid flavour Id in flavour-role configuration");
            }
            if (roleId == null && !bookingConfigurationInput.getRoleIds().isEmpty()) {
                throw new InvalidInputException("Cannot have 'all users' role in flavour-role configuration when booking configuration has specific roles");

            } else if (roleId != null && !roleIdsForFlavourRoleConfigurations.contains(roleId)) {
                throw new InvalidInputException("Invalid role Id in flavour-role configuration");
            }
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
