package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.BookingConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class BookingConfigurationRepository extends AbstractRepository<BookingConfiguration> {

    @Inject
    BookingConfigurationRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<BookingConfiguration> getAll() {
        final TypedQuery<BookingConfiguration> query = getEntityManager().createNamedQuery("bookingConfiguration.getAll", BookingConfiguration.class);
        return query.getResultList();
    }

    public BookingConfiguration getByCloudClientId(Long cloudClientId) {
        try {
            TypedQuery<BookingConfiguration> query = getEntityManager().createNamedQuery("bookingConfiguration.getByCloudClientId", BookingConfiguration.class);
            query.setParameter("cloudClientId", cloudClientId);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public BookingConfiguration getById(Long id) {
        try {
            TypedQuery<BookingConfiguration> query = getEntityManager().createNamedQuery("bookingConfiguration.getById", BookingConfiguration.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public void save(final BookingConfiguration bookingConfiguration) {
        if (bookingConfiguration.getId() == null) {
            persist(bookingConfiguration);

        } else {
            merge(bookingConfiguration);
        }
    }
}
