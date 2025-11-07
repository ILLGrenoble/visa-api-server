package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.DevicePool;
import eu.ill.visa.core.entity.enumerations.DeviceType;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import eu.ill.visa.persistence.repositories.DevicePoolRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

@Transactional
@Singleton
public class DevicePoolService {

    private final DevicePoolRepository repository;
    private final HypervisorService hypervisorService;

    @Inject
    public DevicePoolService(final DevicePoolRepository repository,
                             final HypervisorService hypervisorService) {
        this.repository = repository;
        this.hypervisorService = hypervisorService;
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

    public List<DevicePoolUsage> getDevicePoolUsage() {
        List<DevicePoolUsage> devicePoolUsages = this.repository.getDevicePoolUsage();
        List<HypervisorService.Resource> totalResources = this.hypervisorService.getTotalResources();
        return devicePoolUsages.stream()
            .map(devicePoolUsage ->  {
                if (devicePoolUsage.getTotalUnits() == -1 && devicePoolUsage.getResourceClass() != null) {
                    HypervisorService.Resource resource = totalResources.stream()
                        .filter(aResource -> aResource.getResourceClass().equals(devicePoolUsage.getResourceClass()))
                        .findFirst()
                        .orElse(null);
                    if (resource != null) {
                        return new DevicePoolUsage(devicePoolUsage.getDevicePoolId(), devicePoolUsage.getCloudId(), devicePoolUsage.getDevicePoolName(), devicePoolUsage.getResourceClass(), resource.getTotal(), resource.getUsage());
                    }
                }
                return devicePoolUsage;
            })
            .toList();
    }

    public void save(@NotNull DevicePool devicePool) {
        this.repository.save(devicePool);
    }

    public void delete(@NotNull DevicePool devicePool) {
        devicePool.setDeletedAt(new Date());
        this.save(devicePool);
    }
}
