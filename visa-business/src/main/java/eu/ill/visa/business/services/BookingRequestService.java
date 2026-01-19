package eu.ill.visa.business.services;

import eu.ill.visa.business.notification.EmailManager;
import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.BookingRequestHistory;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import eu.ill.visa.persistence.repositories.BookingRequestRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional
@Singleton
public class BookingRequestService {
    private static final Logger logger = LoggerFactory.getLogger(BookingRequestService.class);

    private final BookingRequestRepository repository;
    private final BookingTokenService bookingTokenService;
    private final EmailManager emailManager;

    @Inject
    public BookingRequestService(final BookingRequestRepository repository,
                                 final BookingTokenService  bookingTokenService,
                                 final EmailManager emailManager) {
        this.repository = repository;
        this.bookingTokenService = bookingTokenService;
        this.emailManager = emailManager;
    }

    public BookingRequest getById(Long id) {
        return this.repository.getById(id);
    }

    public BookingRequest getByUid(String uid) {
        return this.repository.getByUid(uid);
    }

    public List<BookingRequest> getAll() {
        return this.repository.getAll();
    }

    public List<BookingRequest> getAllForOwner(final User owner) {
        return this.repository.getAllForOwnerId(owner.getId());
    }

    public List<BookingRequest> getAllHistoricForOwnerId(final User owner) {
        return this.repository.getAllHistoricForOwnerId(owner.getId());
    }

    public List<BookingRequest> getAllPending() {
        return this.repository.getAllPending();
    }

    public void create(final BookingRequest bookingRequest) {
        this.save(bookingRequest);

        // Create the tokens
        this.bookingTokenService.createBookingTokensForBookingRequest(bookingRequest);

        // Email admin
        this.emailManager.sendBookingRequestCreatedToAdmin(bookingRequest);

        // Email organiser
        this.emailManager.sendBookingRequestCreatedToOwner(bookingRequest);

    }

    public void save(@NotNull BookingRequest bookingRequest) {
        this.repository.save(bookingRequest);
    }

    public void delete(@NotNull BookingRequest bookingRequest, @NotNull User user) {
        bookingRequest.setDeletedAt(new Date());
        bookingRequest.setState(BookingRequestState.DELETED);
        bookingRequest.getHistory().add(new BookingRequestHistory(BookingRequestState.DELETED, null, user));
        this.save(bookingRequest);
    }

    public String getUID() {
        String regex = "^.*[a-zA-Z]+.*";
        Pattern pattern = Pattern.compile(regex);

        do {
            String uid = RandomStringUtils.randomAlphanumeric(8);

            // Ensure UID has at least one character to make it distinguishable from a valid ID
            Matcher matcher = pattern.matcher(uid);

            if (matcher.matches() && this.repository.getByUid(uid) == null) {
                return uid;
            }
        } while (true);
    }

    public BookingRequestHistory acceptBookingRequest(final BookingRequest bookingRequest, final User actor, final String comments) {
        bookingRequest.setState(BookingRequestState.ACCEPTED);

        BookingRequestHistory history = new BookingRequestHistory(BookingRequestState.ACCEPTED, comments, actor);
        bookingRequest.getHistory().add(history);

        this.save(bookingRequest);

        // Email organiser
        this.emailManager.sendBookingRequestValidated(bookingRequest);

        return history;
    }

    public BookingRequestHistory refuseBookingRequest(final BookingRequest bookingRequest, final User actor, final String comments) {
        bookingRequest.setState(BookingRequestState.REFUSED);

        BookingRequestHistory history = new BookingRequestHistory(BookingRequestState.REFUSED, comments, actor);
        bookingRequest.getHistory().add(history);

        this.save(bookingRequest);

        // Email organiser
        this.emailManager.sendBookingRequestValidated(bookingRequest);

        return history;
    }

}
