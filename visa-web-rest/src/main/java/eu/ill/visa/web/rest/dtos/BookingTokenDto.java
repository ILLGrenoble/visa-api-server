package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.BookingToken;

public class BookingTokenDto {

    private final Long id;
    private final String uid;
    private final BookingRequestSimpleDto bookingRequest;
    private final FlavourDto flavour;
    private final UserDto owner;
    private final Long instanceId;

    public BookingTokenDto(BookingToken bookingToken) {
        this.id = bookingToken.getId();
        this.uid = bookingToken.getUid();
        this.bookingRequest = new BookingRequestSimpleDto(bookingToken.getBookingRequest());
        this.flavour = new FlavourDto(bookingToken.getFlavour());
        this.owner = bookingToken.getOwner() == null ? null : new UserDto(bookingToken.getOwner());
        this.instanceId = bookingToken.getInstance() == null ? null : bookingToken.getInstance().getId();
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public BookingRequestSimpleDto getBookingRequest() {
        return bookingRequest;
    }

    public FlavourDto getFlavour() {
        return flavour;
    }

    public UserDto getOwner() {
        return owner;
    }

    public Long getInstanceId() {
        return instanceId;
    }
}
