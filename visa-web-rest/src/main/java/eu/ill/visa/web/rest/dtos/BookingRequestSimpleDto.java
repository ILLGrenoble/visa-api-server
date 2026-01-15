package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.BookingRequest;

import java.time.ZoneId;
import java.util.Date;

public class BookingRequestSimpleDto {

    private final Long id;
    private final String name;
    private final Date startDate;
    private final Date endDate;
    private final UserDto owner;

    public BookingRequestSimpleDto(BookingRequest bookingRequest) {
        this.id = bookingRequest.getId();
        this.name = bookingRequest.getName();
        this.startDate = Date.from(bookingRequest.getStartDate().atZone(ZoneId.systemDefault()).toInstant());
        this.endDate = Date.from(bookingRequest.getEndDate().atZone(ZoneId.systemDefault()).toInstant());
        this.owner = new UserDto(bookingRequest.getOwner());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public UserDto getOwner() {
        return owner;
    }
}
