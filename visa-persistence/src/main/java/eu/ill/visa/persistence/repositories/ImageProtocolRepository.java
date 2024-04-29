package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.ImageProtocol;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class ImageProtocolRepository extends AbstractRepository<ImageProtocol> {

    private static final String FIXTURES_FILE = "fixtures/protocols.sql";

    @Inject
    ImageProtocolRepository(final EntityManager entityManager) {
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
