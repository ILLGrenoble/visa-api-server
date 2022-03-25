package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.ApplicationCredential;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class ApplicationCredentialRepository extends AbstractRepository<ApplicationCredential> {

    @Inject
    ApplicationCredentialRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    public List<ApplicationCredential> getAll() {
        final TypedQuery<ApplicationCredential> query = getEntityManager().createNamedQuery("applicationCredential.getAll", ApplicationCredential.class);
        return query.getResultList();
    }

    public ApplicationCredential getById(Long id) {
        try {
            TypedQuery<ApplicationCredential> query = getEntityManager().createNamedQuery("applicationCredential.getById", ApplicationCredential.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public ApplicationCredential getByApplicationId(String applicationId) {
        try {
            TypedQuery<ApplicationCredential> query = getEntityManager().createNamedQuery("applicationCredential.getByApplicationId", ApplicationCredential.class);
            query.setParameter("applicationId", applicationId);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public void save(final ApplicationCredential applicationCredential) {
        if (applicationCredential.getId() == null) {
            persist(applicationCredential);

        } else {
            merge(applicationCredential);
        }
    }
}
