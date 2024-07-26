package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.ClientAuthenticationToken;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class ClientAuthenticationTokenRepository extends AbstractRepository<ClientAuthenticationToken> {

    @Inject
    ClientAuthenticationTokenRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<ClientAuthenticationToken> getAll() {
        TypedQuery<ClientAuthenticationToken> query = getEntityManager().createNamedQuery("clientAuthenticationToken.getAll", ClientAuthenticationToken.class);
        return query.getResultList();
    }

    public ClientAuthenticationToken getByTokenAndClientId(final String token, final String clientId) {
        try {
            final TypedQuery<ClientAuthenticationToken> query = getEntityManager().createNamedQuery("clientAuthenticationToken.getByTokenAndClientId", ClientAuthenticationToken.class);
            query.setParameter("token", token);
            query.setParameter("clientId", clientId);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void save(ClientAuthenticationToken clientAuthenticationToken) {
        if (clientAuthenticationToken.getId() == null) {
            persist(clientAuthenticationToken);

        } else {
            merge(clientAuthenticationToken);
        }
    }

    public void delete(final ClientAuthenticationToken clientAuthenticationToken) {
        remove(clientAuthenticationToken);
    }
}
