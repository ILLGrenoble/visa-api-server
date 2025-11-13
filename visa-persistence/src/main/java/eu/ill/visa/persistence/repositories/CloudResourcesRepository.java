package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.CloudResources;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class CloudResourcesRepository extends AbstractRepository<CloudResources> {

    @Inject
    CloudResourcesRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<CloudResources> getAll() {
        final TypedQuery<CloudResources> query = getEntityManager().createNamedQuery("cloudResources.getAll", CloudResources.class);
        return query.getResultList();
    }

    public void save(final CloudResources cloudResources) {
        if (cloudResources.getId() == null) {
            persist(cloudResources);

        } else {
            merge(cloudResources);
        }
    }

    public void delete(final CloudResources cloudResources) {
        this.remove(cloudResources);
    }

}
