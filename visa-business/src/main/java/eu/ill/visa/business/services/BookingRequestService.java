package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.BookingRequestHistory;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import eu.ill.visa.persistence.repositories.BookingRequestRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@Transactional
@Singleton
public class BookingRequestService {
    private static final Logger logger = LoggerFactory.getLogger(BookingRequestService.class);

    private final BookingRequestRepository repository;

    @Inject
    public BookingRequestService(final BookingRequestRepository repository) {
        this.repository = repository;
    }

    public BookingRequest getById(Long id) {
        return this.repository.getById(id);
    }

    public List<BookingRequest> getAll() {
        return this.repository.getAll();
    }


    public List<BookingRequest> getAllForOwnerId(final User owner) {
        return this.repository.getAllForOwnerId(owner.getId());
    }

    public List<BookingRequest> getAllHistoricForOwnerId(final User owner) {
        return this.repository.getAllHistoricForOwnerId(owner.getId());
    }

    public List<BookingRequest> getAllPending() {
        return this.repository.getAllPending();
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

}
