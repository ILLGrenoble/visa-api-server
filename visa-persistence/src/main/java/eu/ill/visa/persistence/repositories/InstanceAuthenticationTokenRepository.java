package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class InstanceAuthenticationTokenRepository extends AbstractRepository<InstanceAuthenticationToken> {

    @Inject
    InstanceAuthenticationTokenRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<InstanceAuthenticationToken> getAll() {
        TypedQuery<InstanceAuthenticationToken> query = getEntityManager().createNamedQuery("instanceAuthenticationToken.getAll", InstanceAuthenticationToken.class);
        return query.getResultList();
    }

    public InstanceAuthenticationToken getByToken(final String token) {
        try {
            final TypedQuery<InstanceAuthenticationToken> query = getEntityManager().createNamedQuery("instanceAuthenticationToken.getByToken", InstanceAuthenticationToken.class);
            query.setParameter("token", token);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void save(InstanceAuthenticationToken instanceAuthenticationToken) {
        if (instanceAuthenticationToken.getId() == null) {
            persist(instanceAuthenticationToken);

        } else {
            merge(instanceAuthenticationToken);
        }
    }

    public void delete(final InstanceAuthenticationToken instanceAuthenticationToken) {
        remove(instanceAuthenticationToken);
    }
}
