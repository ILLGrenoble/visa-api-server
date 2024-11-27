package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Image;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class ImageRepository extends AbstractRepository<Image> {


    @Inject
    ImageRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Image> getAll() {
        final TypedQuery<Image> query = getEntityManager().createNamedQuery("image.getAll", Image.class);
        return query.getResultList();
    }

    public List<Image> getAllForAdmin() {
        final TypedQuery<Image> query = getEntityManager().createNamedQuery("image.getAllForAdmin", Image.class);
        return query.getResultList();
    }

    public Long countAllForAdmin() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("image.countAllForAdmin", Long.class);
        return query.getSingleResult();
    }

    public Image getById(final Long id) {
        try {
            final TypedQuery<Image> query = getEntityManager().createNamedQuery("image.getById", Image.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(final Image image) {
        remove(image);
    }

    public void save(final Image image) {
        if (image.getId() == null) {
            persist(image);

        } else {
            merge(image);
        }
    }
}
