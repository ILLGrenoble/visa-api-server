package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.BookingRequestService;
import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.types.BookingRequestType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class BookingRequestResource {

    private static final Logger logger = LoggerFactory.getLogger(BookingRequestResource.class);

    private final BookingRequestService bookingRequestService;

    @Inject
    public BookingRequestResource(final BookingRequestService bookingRequestService) {
        this.bookingRequestService = bookingRequestService;
    }

    @Query
    public @NotNull List<BookingRequestType> bookingRequests() {
        return this.bookingRequestService.getAll().stream()
            .map(BookingRequestType::new)
            .toList();
    }

    @Query
    public @NotNull BookingRequestType bookingRequest(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException  {
        BookingRequest bookingRequest = this.bookingRequestService.getById(id);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Booking Request not found for the given id");
        }

        return new BookingRequestType(bookingRequest);
    }

}
