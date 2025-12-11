package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.BookingRequestFlavour;
import eu.ill.visa.core.entity.BookingRequestHistory;
import eu.ill.visa.core.entity.enumerations.BookingRequestState;

import java.util.Date;
import java.util.List;

public class BookingRequestDto {

    private final Long id;
    private final String name;
    private final Date createdAt;
    private final Date startDate;
    private final Date endDate;
    private final UserDto owner;
    private final BookingRequestState state;
    private final List<BookingRequestFlavourDto> flavours;
    private final List<BookingRequestHistoryDto> history;

    public BookingRequestDto(BookingRequest bookingRequest) {
        this.id = bookingRequest.getId();
        this.name = bookingRequest.getName();
        this.createdAt = bookingRequest.getCreatedAt();
        this.startDate = bookingRequest.getStartDate();
        this.endDate = bookingRequest.getEndDate();
        this.owner = new UserDto(bookingRequest.getOwner());
        this.state = bookingRequest.getState();
        this.flavours = bookingRequest.getFlavours().stream().map(BookingRequestFlavourDto::new).toList();
        this.history = bookingRequest.getHistory().stream().map(BookingRequestHistoryDto::new).toList();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
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

    public BookingRequestState getState() {
        return state;
    }

    public List<BookingRequestFlavourDto> getFlavours() {
        return flavours;
    }

    public List<BookingRequestHistoryDto> getHistory() {
        return history;
    }

    public static final class BookingRequestFlavourDto {
        private final Long id;
        private final FlavourDto flavour;
        private final Long quantity;

        public BookingRequestFlavourDto(BookingRequestFlavour requestFlavour) {
            this.id = requestFlavour.getId();
            this.flavour = new FlavourDto(requestFlavour.getFlavour());
            this.quantity = requestFlavour.getQuantity();
        }

        public Long getId() {
            return id;
        }

        public FlavourDto getFlavour() {
            return flavour;
        }

        public Long getQuantity() {
            return quantity;
        }
    }


    public static final class BookingRequestHistoryDto {
        private final Long id;
        private final BookingRequestState state;
        private final UserDto actor;
        private final String comments;
        private final Date date;

        BookingRequestHistoryDto(BookingRequestHistory requestHistory) {
            this.id = requestHistory.getId();
            this.state = requestHistory.getState();
            this.actor = new UserDto(requestHistory.getActor());
            this.comments = requestHistory.getComments();
            this.date = requestHistory.getCreatedAt();
        }

        public Long getId() {
            return id;
        }

        public BookingRequestState getState() {
            return state;
        }

        public UserDto getActor() {
            return actor;
        }

        public String getComments() {
            return comments;
        }

        public Date getDate() {
            return date;
        }
    }
}
