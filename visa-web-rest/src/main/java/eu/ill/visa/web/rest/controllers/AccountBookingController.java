package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.*;
import eu.ill.visa.business.services.BookingService.BookingRequestValidation;
import eu.ill.visa.core.domain.BookingFlavourConfiguration;
import eu.ill.visa.core.domain.BookingUserConfiguration;
import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.web.rest.dtos.*;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

@Path("/account/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class AccountBookingController extends AbstractController {

    private final BookingService bookingService;
    private final BookingRequestService bookingRequestService;
    private final BookingTokenService  bookingTokenService;
    private final FlavourService flavourService;
    private final FlavourAvailabilityService flavourAvailabilityService;
    private final UserService userService;

    @Inject
    public AccountBookingController(final BookingService bookingService,
                                    final BookingRequestService bookingRequestService,
                                    final BookingTokenService  bookingTokenService,
                                    final FlavourService flavourService,
                                    final FlavourAvailabilityService flavourAvailabilityService,
                                    final UserService userService) {
        this.bookingService = bookingService;
        this.bookingRequestService = bookingRequestService;
        this.bookingTokenService = bookingTokenService;
        this.flavourService = flavourService;
        this.flavourAvailabilityService = flavourAvailabilityService;
        this.userService = userService;
    }

    @GET
    public MetaResponse<List<BookingRequestDto>> getBookings(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);

        return createResponse(this.bookingRequestService.getAllForOwner(user).stream().map(BookingRequestDto::new).toList());
    }

    @POST
    public MetaResponse<BookingRequestDto> create(@Context SecurityContext securityContext, BookingRequestInput input) {
        final User user = this.getUserPrincipal(securityContext);

        BookingRequest bookingRequest = this.convertToBookingRequest(input, user);
        final BookingRequestValidation validation = this.bookingService.validateAndCreateBookingRequest(bookingRequest);

        if (validation.isValid()) {
            return createResponse(new BookingRequestDto(validation.bookingRequest()));

        } else {
            return createResponse(null, validation.errors());
        }
    }

    @GET
    @Path("/{bookingRequest}")
    public MetaResponse<BookingRequestDto> getBookingRequest(@Context SecurityContext securityContext, @PathParam("bookingRequest") BookingRequest bookingRequest) {
        final User user = this.getUserPrincipal(securityContext);
        if (!bookingRequest.getOwner().equals(user)) {
            throw new NotAuthorizedException("You are not allowed to access the booking request");
        }

        return createResponse(new BookingRequestDto(bookingRequest));
    }

    @DELETE
    @Path("/{bookingRequest}")
    public MetaResponse<BookingRequestDto> deleteBookingRequest(@Context SecurityContext securityContext, @PathParam("bookingRequest") BookingRequest bookingRequest) {
        final User user = this.getUserPrincipal(securityContext);
        if (!bookingRequest.getOwner().equals(user)) {
            throw new NotAuthorizedException("You are not allowed to access the booking request");
        }

        this.bookingRequestService.delete(bookingRequest, user);

        return createResponse(new BookingRequestDto(bookingRequest));
    }

    @GET
    @Path("/{bookingRequest}/tokens")
    public MetaResponse<List<BookingTokenDto>> getBookingRequestTokens(@Context SecurityContext securityContext, @PathParam("bookingRequest") BookingRequest bookingRequest) {
        final User user = this.getUserPrincipal(securityContext);
        if (!bookingRequest.getOwner().equals(user)) {
            throw new NotAuthorizedException("You are not allowed to access the booking request");
        }

        return createResponse(this.bookingTokenService.getAllForBookingRequest(bookingRequest).stream().map(BookingTokenDto::new).toList());
    }

    @PUT
    @Path("/{bookingRequest}/tokens")
    public MetaResponse<List<BookingTokenDto>> updateBookingRequestTokens(@Context SecurityContext securityContext, @PathParam("bookingRequest") BookingRequest bookingRequest, List<BookingTokenInput> tokenInputs) {
        final User user = this.getUserPrincipal(securityContext);
        if (!bookingRequest.getOwner().equals(user)) {
            throw new NotAuthorizedException("You are not allowed to access the booking request");
        }

        List<String> ownerIds = tokenInputs.stream()
            .map(BookingTokenInput::getOwnerId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<User> owners = new ArrayList<>();
        for (String ownerId : ownerIds) {
            final User owner = this.userService.getById(ownerId);
            if (owner == null) {
                throw new BadRequestException(format("User not found with id %s", ownerId));
            }

            owners.add(owner);
        }

        List<BookingToken> tokens = this.bookingTokenService.getAllForBookingRequest(bookingRequest);

        for (BookingTokenInput tokenInput : tokenInputs) {
            final BookingToken token = tokens.stream().filter(aToken -> aToken.getId().equals(tokenInput.getId())).findFirst().orElse(null);
            if (token == null) {
                throw new BadRequestException(format("User token found with id %s", tokenInput.getId()));
            }

            final User owner = tokenInput.getOwnerId() == null ? null : owners.stream().filter(anOwner -> anOwner.getId().equals(tokenInput.getOwnerId())).findFirst().orElse(null);
            token.setOwner(owner);
        }

        this.bookingTokenService.saveAll(tokens);

        return createResponse(this.bookingTokenService.getAllForBookingRequest(bookingRequest).stream().map(BookingTokenDto::new).toList());
    }

    @GET
    @Path("/config")
    public MetaResponse<BookingUserConfigurationDto> get(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        return createResponse(new BookingUserConfigurationDto(this.bookingService.getBookingUserConfiguration(user)));
    }

    @GET
    @Path("/flavours/availabilities")
    public MetaResponse<List<FlavourAvailabilitiesFutureDto>> getFlavourAvailabilities(@Context SecurityContext securityContext, @BeanParam AvailabilitiesFilter params) {
        final User user = this.getUserPrincipal(securityContext);

        final BookingUserConfiguration bookingUserConfiguration = this.bookingService.getBookingUserConfiguration(user);
        if (bookingUserConfiguration.flavourConfigurations().isEmpty()) {
            throw new NotAuthorizedException("You are not allowed to get flavour availabilities");
        }

        final List<Flavour> flavours = bookingUserConfiguration.flavourConfigurations().stream()
            .map(BookingFlavourConfiguration::flavour)
            .toList();

        final LocalDate startDate = params.getStartDate();
        final LocalDate endDate = params.getEndDate();

        final Map<Flavour, List<FlavourAvailability>> flavourAvailabilities = this.flavourAvailabilityService.getFutureAvailabilities(flavours, startDate, endDate);
        List<FlavourAvailabilitiesFutureDto> futureAvailabilities = flavourAvailabilities.entrySet().stream().map(entry -> {
            return new FlavourAvailabilitiesFutureDto(entry.getKey(), entry.getValue());
        }).toList();

        return createResponse(futureAvailabilities);
    }

    /**
     * Calculates the dynamic flavour availabilities during the construction of a BookingRequest, taking into account
     * the currently requested instances of specific flavours.
     */
    @POST
    @Path("/flavours/availabilities")
    public MetaResponse<List<FlavourAvailabilitiesFutureDto>> calculateFlavourAvailabilities(@Context SecurityContext securityContext, BookingRequestInput input) {
        final User user = this.getUserPrincipal(securityContext);

        final BookingUserConfiguration bookingUserConfiguration = this.bookingService.getBookingUserConfiguration(user);
        if (bookingUserConfiguration.flavourConfigurations().isEmpty()) {
            throw new NotAuthorizedException("You are not allowed to get flavour availabilities");
        }

        final List<Flavour> flavours = bookingUserConfiguration.flavourConfigurations().stream()
            .map(BookingFlavourConfiguration::flavour)
            .toList();

        BookingRequest bookingRequest = this.convertToBookingRequest(input, user);


        final Map<Flavour, List<FlavourAvailability>> flavourAvailabilities = this.flavourAvailabilityService.calculateFutureAvailabilities(flavours, bookingRequest);
        List<FlavourAvailabilitiesFutureDto> futureAvailabilities = flavourAvailabilities.entrySet().stream().map(entry -> {
            return new FlavourAvailabilitiesFutureDto(entry.getKey(), entry.getValue());
        }).toList();

        return createResponse(futureAvailabilities);
    }

    private BookingRequest convertToBookingRequest(BookingRequestInput input, User user) {
        // Verify that flavours exist
        List<Flavour> flavours = new ArrayList<>();
        input.getFlavourRequests().forEach(flavourRequest -> {
            Flavour flavour = this.flavourService.getById(flavourRequest.getFlavourId());
            if (flavour == null) {
                throw new BadRequestException("Invalid flavour id " + flavourRequest.getFlavourId());
            }
            flavours.add(flavour);
        });

        List<BookingRequestFlavour> flavourRequests = input.getFlavourRequests().stream()
            .map(flavourInput -> {
                final Flavour flavour = flavours.stream().filter(aFlavour -> aFlavour.getId().equals(flavourInput.getFlavourId())).findFirst().orElse(null);
                return new BookingRequestFlavour(flavour, flavourInput.getQuantity());
            }).toList();

        return BookingRequest.Builder()
            .uid(this.bookingRequestService.getUID())
            .name(input.getName())
            .startDate(input.getStartDate().atStartOfDay())
            .endDate(input.getEndDate().atStartOfDay())
            .owner(user)
            .comments(input.getComments())
            .flavours(flavourRequests)
            .build();
    }

    public static final class AvailabilitiesFilter {

        @QueryParam("startDate")
        private LocalDate startDate;

        @QueryParam("endDate")
        private LocalDate endDate;

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }
    }
}
