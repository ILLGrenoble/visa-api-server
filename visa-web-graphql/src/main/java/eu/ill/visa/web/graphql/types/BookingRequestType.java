package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.BookingRequestFlavour;
import eu.ill.visa.core.entity.BookingRequestHistory;
import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;
import java.util.List;

@Type("BookingRequest")
public class BookingRequestType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String uid;
    private final @NotNull String name;
    private final @NotNull Date createdAt;
    private final @NotNull String startDate;
    private final @NotNull String endDate;
    private final @NotNull UserType owner;
    private final @NotNull BookingRequestState state;
    private final @NotNull List<BookingRequestFlavourType> flavours;
    private final @NotNull List<BookingRequestHistoryType> history;

    public BookingRequestType(BookingRequest bookingRequest) {
        this.id = bookingRequest.getId();
        this.uid = bookingRequest.getUid();
        this.name = bookingRequest.getName();
        this.createdAt = bookingRequest.getCreatedAt();
        this.startDate = bookingRequest.getStartDate().toString();
        this.endDate = bookingRequest.getEndDate().toString();
        this.owner = new UserType(bookingRequest.getOwner());
        this.state = bookingRequest.getState();
        this.flavours = bookingRequest.getFlavours().stream().map(BookingRequestFlavourType::new).toList();
        this.history = bookingRequest.getHistory().stream().map(BookingRequestHistoryType::new).toList();
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public UserType getOwner() {
        return owner;
    }

    public BookingRequestState getState() {
        return state;
    }

    public List<BookingRequestFlavourType> getFlavours() {
        return flavours;
    }

    public List<BookingRequestHistoryType> getHistory() {
        return history;
    }

    public static final class BookingRequestFlavourType {
        @AdaptToScalar(Scalar.Int.class)
        private final @NotNull Long id;
        private final @NotNull FlavourType flavour;
        @AdaptToScalar(Scalar.Int.class)
        private final @NotNull Long quantity;

        public BookingRequestFlavourType(BookingRequestFlavour requestFlavour) {
            this.id = requestFlavour.getId();
            this.flavour = new FlavourType(requestFlavour.getFlavour());
            this.quantity = requestFlavour.getQuantity();
        }

        public Long getId() {
            return id;
        }

        public FlavourType getFlavour() {
            return flavour;
        }

        public Long getQuantity() {
            return quantity;
        }
    }


    public static final class BookingRequestHistoryType {
        @AdaptToScalar(Scalar.Int.class)
        private final @NotNull Long id;
        private final @NotNull BookingRequestState state;
        private final @NotNull UserType actor;
        private final String comments;
        private final @NotNull Date date;

        public BookingRequestHistoryType(BookingRequestHistory requestHistory) {
            this.id = requestHistory.getId();
            this.state = requestHistory.getState();
            this.actor = new UserType(requestHistory.getActor());
            this.comments = requestHistory.getComments();
            this.date = requestHistory.getCreatedAt();
        }

        public Long getId() {
            return id;
        }

        public BookingRequestState getState() {
            return state;
        }

        public UserType getActor() {
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
