package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.DevicePool;
import eu.ill.visa.core.entity.enumerations.DeviceType;
import eu.ill.visa.persistence.repositories.DevicePoolRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class DevicePoolService {

    private final DevicePoolRepository repository;

    @Inject
    public DevicePoolService(DevicePoolRepository repository) {
        this.repository = repository;
    }

    public List<DevicePool> getAll() {
        return this.repository.getAll();
    }

    public DevicePool getById(Long id) {
        return this.repository.getById(id);
    }

    public DevicePool getComputeIdentifierAndType(String computeIdentifier, DeviceType deviceType) {
        return this.repository.getComputeIdentifierAndType(computeIdentifier, deviceType);
    }

    public void save(@NotNull DevicePool devicePool) {
        this.repository.save(devicePool);
    }

    public void create(DevicePool devicePool) {
        this.repository.create(devicePool);
    }
}
