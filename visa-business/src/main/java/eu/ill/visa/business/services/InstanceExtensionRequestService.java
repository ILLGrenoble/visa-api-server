package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceExtensionRequest;
import eu.ill.visa.persistence.repositories.InstanceExtensionRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

@Transactional
@Singleton
public class InstanceExtensionRequestService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceExtensionRequestService.class);

    private final InstanceExtensionRequestRepository repository;

    @Inject
    public InstanceExtensionRequestService(final InstanceExtensionRequestRepository repository) {
        this.repository = repository;
    }

    public List<InstanceExtensionRequest> getAll() {
        return this.repository.getAll();
    }

    public InstanceExtensionRequest getForInstance(Instance instance) {
        return this.repository.getForInstance(instance);
    }

    public void save(@NotNull InstanceExtensionRequest instanceMember) {
        this.repository.save(instanceMember);
    }

    public InstanceExtensionRequest create(Instance instance, String comments) {
        InstanceExtensionRequest request = new InstanceExtensionRequest(instance, comments);
        this.save(request);

        // Send email

        return request;
    }
}
