package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.DevicePool;
import eu.ill.visa.core.entity.enumerations.DeviceType;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class DevicePoolRepository extends AbstractRepository<DevicePool> {

    @Inject
    DevicePoolRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<DevicePool> getAll() {
        final TypedQuery<DevicePool> query = getEntityManager().createNamedQuery("devicePool.getAll", DevicePool.class);
        return query.getResultList();
    }

    public DevicePool getById(Long id) {
        try {
            TypedQuery<DevicePool> query = getEntityManager().createNamedQuery("devicePool.getById", DevicePool.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public DevicePool getComputeIdentifierAndType(String computeIdentifier, DeviceType deviceType) {
        try {
            TypedQuery<DevicePool> query = getEntityManager().createNamedQuery("devicePool.getComputeIdentifierAndType", DevicePool.class);
            query.setParameter("computeIdentifier", computeIdentifier);
            query.setParameter("deviceType", deviceType);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public List<DevicePoolUsage> getDevicePoolUsage() {
        final TypedQuery<DevicePoolUsage> query = getEntityManager().createNamedQuery("devicePool.getDevicePoolUsage", DevicePoolUsage.class);
        return query.getResultList();
    }

    public void save(final DevicePool devicePool) {
        if (devicePool.getId() == null) {
            persist(devicePool);

        } else {
            merge(devicePool);
        }
    }

    public void create(DevicePool devicePool) {
        persist(devicePool);
    }
}
