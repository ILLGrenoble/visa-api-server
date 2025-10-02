package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.InstanceDeviceAllocation;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class InstanceDeviceAllocationRepository extends AbstractRepository<InstanceDeviceAllocation> {

    @Inject
    InstanceDeviceAllocationRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceDeviceAllocation> getAll() {
        final TypedQuery<InstanceDeviceAllocation> query = getEntityManager().createNamedQuery("instanceDeviceAllocation.getAll", InstanceDeviceAllocation.class);
        return query.getResultList();
    }

    public List<InstanceDeviceAllocation> getAllByInstanceId(final Long instanceId) {
        final TypedQuery<InstanceDeviceAllocation> query = getEntityManager().createNamedQuery("instanceDeviceAllocation.getAllByInstanceId", InstanceDeviceAllocation.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public List<InstanceDeviceAllocation> getAllByInstanceIds(final List<Long> instanceIds) {
        final TypedQuery<InstanceDeviceAllocation> query = getEntityManager().createNamedQuery("instanceDeviceAllocation.getAllByInstanceIds", InstanceDeviceAllocation.class);
        query.setParameter("instanceIds", instanceIds);
        return query.getResultList();
    }
}
