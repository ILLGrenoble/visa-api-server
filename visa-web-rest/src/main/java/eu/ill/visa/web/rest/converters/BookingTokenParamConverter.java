package eu.ill.visa.web.rest.converters;


import eu.ill.visa.business.services.BookingTokenService;
import eu.ill.visa.core.entity.BookingToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
public class BookingTokenParamConverter implements ParamConverter<BookingToken> {

    private final BookingTokenService bookingTokenService;

    @Inject
    public BookingTokenParamConverter(final BookingTokenService bookingTokenService) {
        this.bookingTokenService = bookingTokenService;
    }

    @Override
    public BookingToken fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final BookingToken bookingToken = this.bookingTokenService.getById(id);
            if (bookingToken == null) {
                throw new NotFoundException("Booking Token not found");
            }
            return bookingToken;

        } else if (value.matches("[a-zA-Z0-9]+")) {
            final BookingToken bookingToken = this.bookingTokenService.getByUid(value);
            if (bookingToken == null) {
                throw new NotFoundException("Booking Token not found");
            }
            return bookingToken;
        }
        throw new NotFoundException("Booking Token not found");
    }

    @Override
    public String toString(final BookingToken value) {
        return value.getUid();
    }
}
