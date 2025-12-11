package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.BookingRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class BookingRequestRepository extends AbstractRepository<BookingRequest> {

    @Inject
    BookingRequestRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public BookingRequest getById(Long id) {
        final TypedQuery<BookingRequest> query = getEntityManager().createNamedQuery("bookingRequest.getById", BookingRequest.class);
        query.setParameter("id", id);
        try {
            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public BookingRequest getByUid(String uid) {
        final TypedQuery<BookingRequest> query = getEntityManager().createNamedQuery("bookingRequest.getByUid", BookingRequest.class);
        query.setParameter("uid", uid);
        try {
            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<BookingRequest> getAll() {
        final TypedQuery<BookingRequest> query = getEntityManager().createNamedQuery("bookingRequest.getAll", BookingRequest.class);
        return query.getResultList();
    }

    public List<BookingRequest> getAllForOwnerId(final String ownerId) {
        final TypedQuery<BookingRequest> query = getEntityManager().createNamedQuery("bookingRequest.getAllForOwner", BookingRequest.class);
        query.setParameter("ownerId", ownerId);

        return query.getResultList();
    }

    public List<BookingRequest> getAllHistoricForOwnerId(final String ownerId) {
        final TypedQuery<BookingRequest> query = getEntityManager().createNamedQuery("bookingRequest.getAllHistoricForOwner", BookingRequest.class);
        query.setParameter("ownerId", ownerId);

        return query.getResultList();
    }

    public List<BookingRequest> getAllPending() {
        final TypedQuery<BookingRequest> query = getEntityManager().createNamedQuery("bookingRequest.getAllPending", BookingRequest.class);
        return query.getResultList();
    }

    public void save(final BookingRequest bookingRequest) {
        if (bookingRequest.getId() == null) {
            persist(bookingRequest);

        } else {
            merge(bookingRequest);
        }
    }

}
