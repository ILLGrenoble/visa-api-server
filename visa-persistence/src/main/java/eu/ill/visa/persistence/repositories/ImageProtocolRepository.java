package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.ImageProtocol;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class ImageProtocolRepository extends AbstractRepository<ImageProtocol> {

    private static final String FIXTURES_FILE = "fixtures/protocols.sql";

    @Inject
    ImageProtocolRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public void initialise() {
        this.initialiseData(FIXTURES_FILE);
    }

    public List<ImageProtocol> getAll() {
        final TypedQuery<ImageProtocol> query = getEntityManager().createNamedQuery("imageProtocol.getAll", ImageProtocol.class);
        return query.getResultList();
    }

    public ImageProtocol getById(final Long id) {
        try {
            final TypedQuery<ImageProtocol> query = getEntityManager().createNamedQuery("imageProtocol.getById", ImageProtocol.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(final ImageProtocol imageProtocol) {
        remove(imageProtocol);
    }

    public void save(final ImageProtocol imageProtocol) {
        if (imageProtocol.getId() == null) {
            persist(imageProtocol);

        } else {
            merge(imageProtocol);
        }
    }

    public ImageProtocol getByName(String name) {
        try {
            final TypedQuery<ImageProtocol> query = getEntityManager().createNamedQuery("imageProtocol.getByName", ImageProtocol.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }
}
