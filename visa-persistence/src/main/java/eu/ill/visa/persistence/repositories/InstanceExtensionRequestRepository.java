package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceExtensionRequest;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class InstanceExtensionRequestRepository extends AbstractRepository<InstanceExtensionRequest> {

    @Inject
    InstanceExtensionRequestRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceExtensionRequest> getAll() {
        final TypedQuery<InstanceExtensionRequest> query = getEntityManager().createNamedQuery("instanceExtensionRequest.getAll", InstanceExtensionRequest.class);
        return query.getResultList();
    }

    public InstanceExtensionRequest getById(Long id) {
        try {
            TypedQuery<InstanceExtensionRequest> query = getEntityManager().createNamedQuery("instanceExtensionRequest.getById", InstanceExtensionRequest.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
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
