package eu.ill.visa.web.rest.dtos;


import eu.ill.visa.core.entity.BookingRequest;

import java.util.List;

public class AccountBookingRequestsDto {
    private final List<BookingRequestDto> ownerBookingRequests;
    private final List<BookingRequestDto> organiserBookingRequests;

    public AccountBookingRequestsDto(List<BookingRequest> ownerBookingRequests, List<BookingRequest> organiserBookingRequests) {
        this.ownerBookingRequests = ownerBookingRequests.stream().map(BookingRequestDto::new).toList();
        this.organiserBookingRequests = organiserBookingRequests.stream().map(BookingRequestDto::new).toList();
    }

    public List<BookingRequestDto> getOwnerBookingRequests() {
        return ownerBookingRequests;
    }

    public List<BookingRequestDto> getOrganiserBookingRequests() {
        return organiserBookingRequests;
    }
}
