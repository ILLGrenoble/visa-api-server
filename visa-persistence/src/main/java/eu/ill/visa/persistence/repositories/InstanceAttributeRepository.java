package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.InstanceAttribute;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class InstanceAttributeRepository {

    private final EntityManager entityManager;

    @Inject
    InstanceAttributeRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<InstanceAttribute> getAllForInstanceId(Long instanceId) {
        TypedQuery<InstanceAttribute> query = this.entityManager.createNamedQuery("instanceAttribute.getAllByInstanceId", InstanceAttribute.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }
}
