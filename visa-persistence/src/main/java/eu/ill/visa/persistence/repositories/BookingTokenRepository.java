package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.BookingToken;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class BookingTokenRepository extends AbstractRepository<BookingToken> {

    @Inject
    BookingTokenRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public BookingToken getById(Long id) {
        final TypedQuery<BookingToken> query = getEntityManager().createNamedQuery("bookingToken.getById", BookingToken.class);
        query.setParameter("id", id);
        try {
            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public BookingToken getByUid(String uid) {
        final TypedQuery<BookingToken> query = getEntityManager().createNamedQuery("bookingToken.getByUid", BookingToken.class);
        query.setParameter("uid", uid);
        try {
            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public List<BookingToken> getAllForBookingRequestId(Long bookingRequestId) {
        final TypedQuery<BookingToken> query = getEntityManager().createNamedQuery("bookingToken.getAllForBookingRequestId", BookingToken.class);
        query.setParameter("bookingRequestId", bookingRequestId);
        return query.getResultList();
    }

    public List<BookingToken> getAllActiveUnassigned() {
        final TypedQuery<BookingToken> query = getEntityManager().createNamedQuery("bookingToken.getAllActiveUnassigned", BookingToken.class);
        return query.getResultList();
    }


    public void save(final BookingToken bookingToken) {
        if (bookingToken.getId() == null) {
            persist(bookingToken);

        } else {
            merge(bookingToken);
        }
    }

}
