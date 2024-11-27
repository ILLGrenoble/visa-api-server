package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Flavour;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class FlavourRepository extends AbstractRepository<Flavour> {

    @Inject
    FlavourRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Flavour> getAll() {
        final TypedQuery<Flavour> query = getEntityManager().createNamedQuery("flavour.getAll", Flavour.class);
        return query.getResultList();
    }

    public List<Flavour> getAllForAdmin() {
        final TypedQuery<Flavour> query = getEntityManager().createNamedQuery("flavour.getAllForAdmin", Flavour.class);
        return query.getResultList();
    }

    public Long countAllForAdmin() {
        TypedQuery<Long> query = getEntityManager().createNamedQuery("flavour.countAllForAdmin", Long.class);
        return query.getSingleResult();
    }

    public Flavour getById(Long id) {
        try {
            TypedQuery<Flavour> query = getEntityManager().createNamedQuery("flavour.getById", Flavour.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public void delete(Flavour flavour) {
        remove(flavour);
    }

    public void save(final Flavour flavour) {
        if (flavour.getId() == null) {
            persist(flavour);

        } else {
            merge(flavour);
        }
    }

    public void create(Flavour flavour) {
        persist(flavour);
    }


}
