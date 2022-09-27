package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.CloudProviderConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class CloudProviderRepository extends AbstractRepository<CloudProviderConfiguration> {

    @Inject
    CloudProviderRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    @Override
    public List<CloudProviderConfiguration> getAll() {
        final TypedQuery<CloudProviderConfiguration> query = getEntityManager().createNamedQuery("cloudProviderConfiguration.getAll", CloudProviderConfiguration.class);
        return query.getResultList();
    }

    public CloudProviderConfiguration getById(final Long id) {
        try {
            final TypedQuery<CloudProviderConfiguration> query = getEntityManager().createNamedQuery("cloudProviderConfiguration.getById", CloudProviderConfiguration.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(CloudProviderConfiguration cloudProviderConfiguration) {
        remove(cloudProviderConfiguration);
    }

    public void save(final CloudProviderConfiguration cloudProviderConfiguration) {
        if (cloudProviderConfiguration.getId() == null) {
            persist(cloudProviderConfiguration);

        } else {
            merge(cloudProviderConfiguration);
        }
    }

    public void create(CloudProviderConfiguration cloudProviderConfiguration) {
        persist(cloudProviderConfiguration);
    }
}
