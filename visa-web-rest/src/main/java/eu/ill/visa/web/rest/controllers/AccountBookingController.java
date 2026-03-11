package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.*;
import eu.ill.visa.business.services.BookingService.BookingRequestValidation;
import eu.ill.visa.core.domain.BookingFlavourConfiguration;
import eu.ill.visa.core.domain.BookingUserConfiguration;
import eu.ill.visa.core.domain.FlavourAvailability;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import eu.ill.visa.web.rest.dtos.*;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.time.LocalDate;
import java.util.*;

import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.OWNER;
import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.SUPPORT;
import static java.lang.String.format;

@Path("/account/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class AccountBookingController extends AbstractController {

    private final BookingService bookingService;
    private final BookingRequestService bookingRequestService;
    private final BookingTokenService  bookingTokenService;
    private final FlavourAvailabilityService flavourAvailabilityService;
    private final UserService userService;

    @Inject
    public AccountBookingController(final BookingService bookingService,
                                    final BookingRequestService bookingRequestService,
                                    final BookingTokenService  bookingTokenService,
                                    final FlavourAvailabilityService flavourAvailabilityService,
                                    final UserService userService) {
        this.bookingService = bookingService;
        this.bookingRequestService = bookingRequestService;
        this.bookingTokenService = bookingTokenService;
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

        final BookingUserConfiguration bookingUserConfiguration = this.bookingService.getBookingUserConfiguration(user);
        if (bookingUserConfiguration.flavourConfigurations().isEmpty()) {
            throw new NotAuthorizedException("You are not allowed to create instance reservations");
        }

        final List<Flavour> flavours = bookingUserConfiguration.flavourConfigurations().stream()
            .map(BookingFlavourConfiguration::flavour)
            .toList();


        BookingRequest bookingRequest = this.convertToBookingRequest(input, user, flavours);
        final BookingRequestValidation validation = this.bookingService.validateAndSaveBookingRequest(bookingRequest, input.isRequestValidation());

        if (validation.isValid()) {
            return createResponse(new BookingRequestDto(validation.bookingRequest()));

        } else {
            return createResponse(null, validation.errors());
        }
    }

    @PUT
    @Path("/{bookingRequest}")
    public MetaResponse<BookingRequestDto> update(@Context SecurityContext securityContext, @PathParam("bookingRequest") BookingRequest bookingRequest, BookingRequestInput input) {
        final User user = this.getUserPrincipal(securityContext);
        if (!bookingRequest.getOwner().equals(user)) {
            throw new NotAuthorizedException("You are not allowed to update the booking request");
        }

        final BookingUserConfiguration bookingUserConfiguration = this.bookingService.getBookingUserConfiguration(user);
        if (bookingUserConfiguration.flavourConfigurations().isEmpty()) {
            throw new NotAuthorizedException("You are not allowed to create instance reservations");
        }

        if (!input.getUid().equals(bookingRequest.getUid())) {
            throw new BadRequestException("The booking request id does not match the URL for the booking request");
        }

        final List<Flavour> flavours = bookingUserConfiguration.flavourConfigurations().stream()
            .map(BookingFlavourConfiguration::flavour)
            .toList();

        this.updateBookingRequest(bookingRequest, input, flavours);
        final BookingRequestValidation validation = this.bookingService.validateAndSaveBookingRequest(bookingRequest, input.isRequestValidation());

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
    @Path("/tokens")
    public MetaResponse<List<BookingTokenDto>> getAssignedBookingRequestTokens(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);

        return createResponse(this.bookingTokenService.getAllAssignedToUser(user).stream().map(token ->  this.convertToBookingTokenDto(token, user)).toList());
    }

    @GET
    @Path("/tokens/{bookingToken}")
    public MetaResponse<BookingTokenDto> getAssignedBookingRequestToken(@Context SecurityContext securityContext, @PathParam("bookingToken") BookingToken bookingToken) {
        final User user = this.getUserPrincipal(securityContext);
        if (!bookingToken.getOwner().equals(user)) {
            throw new NotAuthorizedException("You are not allowed to access the booking token");
        }

        return createResponse(this.convertToBookingTokenDto(bookingToken, user));
    }

    @GET
    @Path("/{bookingRequest}/tokens")
    public MetaResponse<List<BookingTokenDto>> getBookingRequestTokens(@Context SecurityContext securityContext, @PathParam("bookingRequest") BookingRequest bookingRequest) {
        final User user = this.getUserPrincipal(securityContext);
        if (!bookingRequest.getOwner().equals(user)) {
            throw new NotAuthorizedException("You are not allowed to access the booking request");
        }

        return createResponse(this.bookingTokenService.getAllForBookingRequest(bookingRequest).stream().map(token ->  this.convertToBookingTokenDto(token, user)).toList());
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

        Map<Long, User> tokenIdOwners = new HashMap<>();
        for (BookingTokenInput tokenInput : tokenInputs) {
            final BookingToken token = tokens.stream().filter(aToken -> aToken.getId().equals(tokenInput.getId())).findFirst().orElse(null);
            if (token == null) {
                throw new BadRequestException(format("User token found with id %d", tokenInput.getId()));
            } else if (token.getInstance() != null) {
                throw new NotAuthorizedException(format("Token %d with associated instance cannot change owners", tokenInput.getId()));
            }

            final User owner = tokenInput.getOwnerId() == null ? null : owners.stream().filter(anOwner -> anOwner.getId().equals(tokenInput.getOwnerId())).findFirst().orElse(null);

            tokenIdOwners.put(token.getId(), owner);
        }

        this.bookingTokenService.updateTokenOwners(bookingRequest, tokenIdOwners);

        return createResponse(this.bookingTokenService.getAllForBookingRequest(bookingRequest).stream().map(token ->  this.convertToBookingTokenDto(token, user)).toList());
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

        BookingRequest bookingRequest = null;
        if (input.getUid() != null) {
            bookingRequest = this.bookingRequestService.getByUid(input.getUid());
            if (bookingRequest == null) {
                throw new BadRequestException(format("Booking request not found with uid %s", input.getUid()));
            }

            if (!bookingRequest.getOwner().equals(user)) {
                throw new NotAuthorizedException("You are not the owner of this booking request");
            }

            this.updateBookingRequest(bookingRequest, input, flavours);

        } else {
            bookingRequest = this.convertToBookingRequest(input, user, flavours);
        }


        final Map<Flavour, List<FlavourAvailability>> flavourAvailabilities = this.flavourAvailabilityService.calculateFutureAvailabilities(flavours, bookingRequest);
        List<FlavourAvailabilitiesFutureDto> futureAvailabilities = flavourAvailabilities.entrySet().stream().map(entry -> {
            return new FlavourAvailabilitiesFutureDto(entry.getKey(), entry.getValue());
        }).toList();

        return createResponse(futureAvailabilities);
    }

    private BookingRequest convertToBookingRequest(BookingRequestInput input, User user, List<Flavour> flavours) {
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

    private void updateBookingRequest(BookingRequest bookingRequest, BookingRequestInput input, List<Flavour> flavours) {
        bookingRequest.setName(input.getName());
        bookingRequest.setStartDate(input.getStartDate().atStartOfDay());
        bookingRequest.setEndDate(input.getEndDate().atStartOfDay());
        bookingRequest.getHistory().add(new BookingRequestHistory(BookingRequestState.CREATED, input.getComments(), bookingRequest.getOwner()));

        List<Long> inputFlavourIds = input.getFlavourRequests().stream().map(BookingRequestInput.BookingRequestFlavourInput::getFlavourId).toList();

        List<BookingRequestFlavour> oldFlavourRequests = bookingRequest.getFlavours().stream()
            .filter(bookingRequestFlavour -> inputFlavourIds.contains(bookingRequestFlavour.getFlavour().getId()))
            .toList();

        List<BookingRequestFlavour> flavourRequests = input.getFlavourRequests().stream()
            .map(flavourInput -> {
                BookingRequestFlavour oldFlavourRequest = oldFlavourRequests.stream().filter(aFlavourRequest -> aFlavourRequest.getFlavour().getId().equals(flavourInput.getFlavourId())).findFirst().orElse(null);
                if (oldFlavourRequest == null) {
                    final Flavour flavour = flavours.stream().filter(aFlavour -> aFlavour.getId().equals(flavourInput.getFlavourId())).findFirst().orElse(null);
                    return new BookingRequestFlavour(flavour, flavourInput.getQuantity());

                } else {
                    oldFlavourRequest.setQuantity(flavourInput.getQuantity());
                }

                final Flavour flavour = flavours.stream().filter(aFlavour -> aFlavour.getId().equals(flavourInput.getFlavourId())).findFirst().orElse(null);
                return new BookingRequestFlavour(flavour, flavourInput.getQuantity());
            }).toList();

        bookingRequest.setFlavours(flavourRequests);
    }

    private BookingTokenDto convertToBookingTokenDto(BookingToken token, User user) {

        final Instance instance = token.getInstance();
        BookingTokenDto.BookingTokenInstanceDto instanceDto = null;
        if (instance != null) {
            instanceDto = new BookingTokenDto.BookingTokenInstanceDto(instance);

            if (token.getOwner().equals(user)) {
                instanceDto.setMembership(new InstanceMemberDto(new UserDto(user), OWNER));
                instanceDto.setCanConnectWhileOwnerAway(true);
                instanceDto.setUnrestrictedAccess(true);

            } else {
                instanceDto.setMembership(new InstanceMemberDto(new UserDto(user), SUPPORT));
                boolean canConnectWhenOwnerAway = instance.canAccessWhenOwnerAway() || !token.getOwner().hasRoleWithName(Role.STAFF_ROLE);
                instanceDto.setCanConnectWhileOwnerAway(canConnectWhenOwnerAway);
                instanceDto.setUnrestrictedAccess((instance.getUnrestrictedMemberAccess() != null));
            }
        }

        return new BookingTokenDto(token, instanceDto);
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
