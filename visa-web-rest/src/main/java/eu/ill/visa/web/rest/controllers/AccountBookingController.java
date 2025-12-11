package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.BookingRequestService;
import eu.ill.visa.business.services.BookingService;
import eu.ill.visa.business.services.BookingService.BookingRequestValidation;
import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.BookingRequestFlavour;
import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.rest.dtos.BookingRequestDto;
import eu.ill.visa.web.rest.dtos.BookingRequestInput;
import eu.ill.visa.web.rest.dtos.BookingUserConfigurationDto;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.List;

@Path("/account/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class AccountBookingController extends AbstractController {

    private final BookingService bookingService;
    private final BookingRequestService bookingRequestService;
    private final FlavourService flavourService;

    @Inject
    public AccountBookingController(final BookingService bookingService,
                                    final BookingRequestService bookingRequestService,
                                    final FlavourService flavourService) {
        this.bookingService = bookingService;
        this.bookingRequestService = bookingRequestService;
        this.flavourService = flavourService;
    }

    @GET
    public MetaResponse<List<BookingRequestDto>> getBookings(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);

        return createResponse(this.bookingRequestService.getAllForOwner(user).stream().map(BookingRequestDto::new).toList());
    }

    @POST
    public MetaResponse<BookingRequestDto> create(@Context SecurityContext securityContext, BookingRequestInput input) {
        final User user = this.getUserPrincipal(securityContext);

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

        BookingRequest bookingRequest = BookingRequest.Builder()
            .uid(this.bookingRequestService.getUID())
            .name(input.getName())
            .startDate(input.getStartDate())
            .endDate(input.getEndDate())
            .owner(user)
            .comments(input.getComments())
            .flavours(flavourRequests)
            .build();
        final BookingRequestValidation validation = this.bookingService.validateAndSaveBookingRequest(bookingRequest);

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
    @Path("/config")
    public MetaResponse<BookingUserConfigurationDto> get(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        return createResponse(new BookingUserConfigurationDto(this.bookingService.getBookingUserConfiguration(user)));
    }

}
