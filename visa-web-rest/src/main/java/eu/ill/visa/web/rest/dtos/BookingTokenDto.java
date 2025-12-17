package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.BookingToken;

public class BookingTokenDto {

    private final Long id;
    private final String uid;
    private final FlavourDto flavour;
    private final UserDto owner;
    private final InstanceDto instance;

    public BookingTokenDto(BookingToken bookingToken) {
        this.id = bookingToken.getId();
        this.uid = bookingToken.getUid();
        this.flavour = new FlavourDto(bookingToken.getFlavour());
        this.owner = bookingToken.getOwner() == null ? null : new UserDto(bookingToken.getOwner());
        this.instance = bookingToken.getInstance() == null ? null : new InstanceDto(bookingToken.getInstance());
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public FlavourDto getFlavour() {
        return flavour;
    }

    public UserDto getOwner() {
        return owner;
    }

    public InstanceDto getInstance() {
        return instance;
    }
}
