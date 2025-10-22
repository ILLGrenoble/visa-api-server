package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.FlavourRoleLifetime;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class FlavourRoleLifetimeRepository extends AbstractRepository<FlavourRoleLifetime> {

    @Inject
    FlavourRoleLifetimeRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<FlavourRoleLifetime> getAll() {
        final TypedQuery<FlavourRoleLifetime> query = getEntityManager().createNamedQuery("flavourRoleLifetime.getAll", FlavourRoleLifetime.class);
        return query.getResultList();
    }

    public List<FlavourRoleLifetime> getAllByFlavourId(Long flavourId) {
        final TypedQuery<FlavourRoleLifetime> query = getEntityManager().createNamedQuery("flavourRoleLifetime.getAllByFlavourId", FlavourRoleLifetime.class);
        query.setParameter("flavourId", flavourId);

        return query.getResultList();
    }

    public List<FlavourRoleLifetime> getAllByFlavourIds(List<Long> flavourIds) {
        final TypedQuery<FlavourRoleLifetime> query = getEntityManager().createNamedQuery("flavourRoleLifetime.getAllByFlavourIds", FlavourRoleLifetime.class);
        query.setParameter("flavourIds", flavourIds);

        return query.getResultList();
    }

    public FlavourRoleLifetime getById(Long id) {
        try {
            TypedQuery<FlavourRoleLifetime> query = getEntityManager().createNamedQuery("flavourRoleLifetime.getById", FlavourRoleLifetime.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public void save(final FlavourRoleLifetime flavourRoleLifetime) {
        if (flavourRoleLifetime.getId() == null) {
            persist(flavourRoleLifetime);

        } else {
            merge(flavourRoleLifetime);
        }
    }

    public void delete(final FlavourRoleLifetime flavourRoleLifetime) {
        if (flavourRoleLifetime.getId() != null) {
            remove(flavourRoleLifetime);
        }
    }
}
