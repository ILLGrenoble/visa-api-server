package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.ApplicationCredential;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class ApplicationCredentialRepository extends AbstractRepository<ApplicationCredential> {

    @Inject
    ApplicationCredentialRepository(final EntityManager entityManager) {
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
