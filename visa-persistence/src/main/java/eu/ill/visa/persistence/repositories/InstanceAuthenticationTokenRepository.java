package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.InstanceAuthenticationToken;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class InstanceAuthenticationTokenRepository extends AbstractRepository<InstanceAuthenticationToken> {

    @Inject
    InstanceAuthenticationTokenRepository(final Provider<EntityManager> entityManager) {
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
