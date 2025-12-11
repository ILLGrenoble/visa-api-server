package eu.ill.visa.web.rest.converters;


import eu.ill.visa.business.services.BookingRequestService;
import eu.ill.visa.core.entity.BookingRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
public class BookingRequestParamConverter implements ParamConverter<BookingRequest> {

    private final BookingRequestService bookingRequestService;

    @Inject
    public BookingRequestParamConverter(final BookingRequestService bookingRequestService) {
        this.bookingRequestService = bookingRequestService;
    }

    @Override
    public BookingRequest fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final BookingRequest bookingRequest = this.bookingRequestService.getById(id);
            if (bookingRequest == null) {
                throw new NotFoundException("Booking Request not found");
            }
            return bookingRequest;

        } else if (value.matches("[a-zA-Z0-9]+")) {
            final BookingRequest bookingRequest = this.bookingRequestService.getByUid(value);
            if (bookingRequest == null) {
                throw new NotFoundException("Booking Request not found");
            }
            return bookingRequest;
        }
        throw new NotFoundException("Booking Request not found");
    }

    @Override
    public String toString(final BookingRequest value) {
        return value.getUid();
    }
}
