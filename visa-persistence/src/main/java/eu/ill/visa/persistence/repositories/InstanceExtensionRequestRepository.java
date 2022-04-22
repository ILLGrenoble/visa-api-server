package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceExtensionRequest;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class InstanceExtensionRequestRepository extends AbstractRepository<InstanceExtensionRequest> {

    @Inject
    InstanceExtensionRequestRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<InstanceExtensionRequest> getAll() {
        final TypedQuery<InstanceExtensionRequest> query = getEntityManager().createNamedQuery("instanceExtensionRequest.getAll", InstanceExtensionRequest.class);
        return query.getResultList();
    }


    public InstanceExtensionRequest getForInstance(Instance instance) {
        try {
            TypedQuery<InstanceExtensionRequest> query = getEntityManager().createNamedQuery("instanceExtensionRequest.getForInstance", InstanceExtensionRequest.class);
            query.setParameter("instance", instance);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void save(final InstanceExtensionRequest instanceExtensionRequest) {
        if (instanceExtensionRequest.getId() == null) {
            persist(instanceExtensionRequest);
        } else {
            merge(instanceExtensionRequest);
        }
    }
}
