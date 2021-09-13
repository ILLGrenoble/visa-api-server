package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Flavour;
import eu.ill.visa.core.domain.FlavourLimit;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class FlavourLimitRepository extends AbstractRepository<FlavourLimit> {

    @Inject
    FlavourLimitRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<FlavourLimit> getAll() {
        final TypedQuery<FlavourLimit> query = getEntityManager().createNamedQuery("flavourLimit.getAll", FlavourLimit.class);
        return query.getResultList();
    }

    public List<FlavourLimit> getAllOfTypeForFlavour(Flavour flavour, String type) {
        final TypedQuery<FlavourLimit> query = getEntityManager().createNamedQuery("flavourLimit.getAllOfTypeForFlavour", FlavourLimit.class);
        query.setParameter("flavour", flavour);
        query.setParameter("type", type);
        return query.getResultList();
    }

    public FlavourLimit getById(final Long id) {
        try {
            final TypedQuery<FlavourLimit> query = getEntityManager().createNamedQuery("flavourLimit.getById", FlavourLimit.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(final FlavourLimit flavourLimit) {
        remove(flavourLimit);
    }

    public void save(final FlavourLimit flavourLimit) {
        if (flavourLimit.getId() == null) {
            persist(flavourLimit);
        } else {
            merge(flavourLimit);
        }
    }
}
