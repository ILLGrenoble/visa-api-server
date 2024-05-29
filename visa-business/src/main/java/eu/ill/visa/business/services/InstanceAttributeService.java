package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.InstanceAttribute;
import eu.ill.visa.persistence.repositories.InstanceAttributeRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@Singleton
public class InstanceAttributeService {

    private final InstanceAttributeRepository repository;

    @Inject
    public InstanceAttributeService(final InstanceAttributeRepository repository) {
        this.repository = repository;
    }

    public List<InstanceAttribute> getAllForInstanceId(Long instanceId) {
        return this.repository.getAllForInstanceId(instanceId);
    }


}
