package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.BookingToken;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceState;

import java.util.Date;

public class BookingTokenDto {

    private final Long id;
    private final String uid;
    private final BookingRequestSimpleDto bookingRequest;
    private final FlavourDto flavour;
    private final UserDto owner;
    private final BookingTokenInstanceDto instance;

    public BookingTokenDto(BookingToken bookingToken) {
        this.id = bookingToken.getId();
        this.uid = bookingToken.getUid();
        this.bookingRequest = new BookingRequestSimpleDto(bookingToken.getBookingRequest());
        this.flavour = new FlavourDto(bookingToken.getFlavour());
        this.owner = bookingToken.getOwner() == null ? null : new UserDto(bookingToken.getOwner());
        this.instance = bookingToken.getInstance() == null ? null : new BookingTokenInstanceDto(bookingToken.getInstance());
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

    public BookingTokenInstanceDto getInstance() {
        return instance;
    }

    public static final class BookingTokenInstanceDto {
        private final Long id;
        private final String uid;
        private final String name;
        private final PlanDto plan;
        private final InstanceState state;
        private final Date createdAt;

        public BookingTokenInstanceDto(Instance instance) {
            this.id = instance.getId();
            this.uid = instance.getUid();
            this.name = instance.getName();
            this.plan = new PlanDto(instance.getPlan());
            this.state = instance.getState();
            this.createdAt = instance.getCreatedAt();
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

        public PlanDto getPlan() {
            return plan;
        }

        public InstanceState getState() {
            return state;
        }

        public Date getCreatedAt() {
            return createdAt;
        }
    }
}
