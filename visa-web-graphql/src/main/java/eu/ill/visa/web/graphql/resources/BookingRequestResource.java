package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.BookingRequestService;
import eu.ill.visa.business.services.BookingTokenService;
import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.BookingRequestResponseInput;
import eu.ill.visa.web.graphql.types.BookingRequestType;
import eu.ill.visa.web.graphql.types.BookingTokenType;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class BookingRequestResource {

    private static final Logger logger = LoggerFactory.getLogger(BookingRequestResource.class);

    private final BookingRequestService bookingRequestService;
    private final BookingTokenService bookingTokenService;
    private final SecurityIdentity securityIdentity;

    @Inject
    public BookingRequestResource(final BookingRequestService bookingRequestService,
                                  final BookingTokenService bookingTokenService,
                                  final SecurityIdentity securityIdentity) {
        this.bookingRequestService = bookingRequestService;
        this.bookingTokenService = bookingTokenService;
        this.securityIdentity = securityIdentity;
    }

    @Query
    public @NotNull List<BookingRequestType> bookingRequests() {
        return this.bookingRequestService.getAll().stream()
            .map(BookingRequestType::new)
            .toList();
    }

    @Query
    public @NotNull BookingRequestType bookingRequest(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        BookingRequest bookingRequest = this.bookingRequestService.getById(id);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Booking Request not found for the given id");
        }

        return new BookingRequestType(bookingRequest);
    }

    @Query
    public @NotNull List<BookingTokenType> bookingTokens(@NotNull @AdaptToScalar(Scalar.Int.class) Long bookingRequestId) throws EntityNotFoundException {
        BookingRequest bookingRequest = this.bookingRequestService.getById(bookingRequestId);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Booking Request not found for the given id");
        }

        return this.bookingTokenService.getAllForBookingRequest(bookingRequest).stream()
            .map(BookingTokenType::new)
            .toList();
    }

    @Mutation
    public @NotNull BookingRequestType bookingRequestResponse(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull BookingRequestResponseInput response) throws EntityNotFoundException, InvalidInputException {
        BookingRequest bookingRequest = this.bookingRequestService.getById(id);
        if (bookingRequest == null) {
            throw new EntityNotFoundException("Booking Request not found for the given id");
        }

        if (!bookingRequest.getState().equals(BookingRequestState.CREATED)) {
            throw new InvalidInputException("Booking request is not in a creation state");
        }

        final AccountToken accountToken = (AccountToken) this.securityIdentity.getPrincipal();
        final User user = accountToken.getUser();

        if (response.getAccepted()) {
            this.bookingRequestService.acceptBookingRequest(bookingRequest, user, response.getComments());
        } else {
            this.bookingRequestService.refuseBookingRequest(bookingRequest, user, response.getComments());
        }

        return new BookingRequestType(this.bookingRequestService.getById(id));
    }

}
