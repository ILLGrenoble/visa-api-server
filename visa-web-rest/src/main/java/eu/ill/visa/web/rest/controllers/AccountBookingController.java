package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.BookingService;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.rest.dtos.BookingFlavourConfigurationDto;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/account/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class AccountBookingController extends AbstractController {

    private final BookingService bookingService;

    @Inject
    public AccountBookingController(final BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GET
    @Path("/config")
    public MetaResponse<List<BookingFlavourConfigurationDto>> get(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        return createResponse(this.bookingService.getBookingFlavourConfigurations(user).stream()
            .map(BookingFlavourConfigurationDto::new)
            .toList());
    }

}
