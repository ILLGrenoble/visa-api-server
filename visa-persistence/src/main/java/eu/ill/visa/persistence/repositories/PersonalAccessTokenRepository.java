package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.PersonalAccessToken;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class PersonalAccessTokenRepository extends AbstractRepository<PersonalAccessToken> {

    @Inject
    PersonalAccessTokenRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public PersonalAccessToken getByInstanceAndId(final Instance instance, final Long id) {
        try {
            final TypedQuery<PersonalAccessToken> query = getEntityManager().createNamedQuery("personalAccessToken.getByInstanceAndId", PersonalAccessToken.class);
            query.setParameter("instance", instance);
            query.setParameter("id", id);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public PersonalAccessToken getByInstanceAndToken(final Instance instance, final String token) {
        try {
            final TypedQuery<PersonalAccessToken> query = getEntityManager().createNamedQuery("personalAccessToken.getByInstanceAndToken", PersonalAccessToken.class);
            query.setParameter("instance", instance);
            query.setParameter("token", token);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<PersonalAccessToken> getAllForInstance(final Instance instance) {
        TypedQuery<PersonalAccessToken> query = getEntityManager().createNamedQuery("personalAccessToken.getAllForInstance", PersonalAccessToken.class);
        query.setParameter("instance", instance);
        return query.getResultList();
    }

    public void save(PersonalAccessToken personalAccessToken) {
        if (personalAccessToken.getId() == null) {
            persist(personalAccessToken);

        } else {
            merge(personalAccessToken);
        }
    }
}
