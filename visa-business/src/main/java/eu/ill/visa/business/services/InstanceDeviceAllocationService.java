package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.InstanceDeviceAllocation;
import eu.ill.visa.persistence.repositories.InstanceDeviceAllocationRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@Singleton
public class InstanceDeviceAllocationService {

    private final InstanceDeviceAllocationRepository repository;

    @Inject
    public InstanceDeviceAllocationService(InstanceDeviceAllocationRepository repository) {
        this.repository = repository;
    }

    public List<InstanceDeviceAllocation> getAll() {
        return this.repository.getAll();
    }

    public List<InstanceDeviceAllocation> getAllByInstanceId(final Long instanceId) {
        return this.repository.getAllByInstanceId(instanceId);
    }

    public List<List<InstanceDeviceAllocation>> getAllByInstanceIds(final List<Long> instanceIds) {
        List<InstanceDeviceAllocation> ungroupedInstanceDeviceAllocations = this.repository.getAllByInstanceIds(instanceIds);
        return instanceIds.stream().map(id -> {
            return ungroupedInstanceDeviceAllocations.stream().filter(instanceDeviceAllocation -> instanceDeviceAllocation.getInstance().getId().equals(id)).toList();
        }).toList();
    }
}
