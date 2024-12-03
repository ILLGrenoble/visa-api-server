package eu.ill.visa.business.services;

import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.core.entity.InstanceActivity;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.persistence.repositories.InstanceActivityRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Transactional
@Singleton
public class InstanceActivityService {

    private final InstanceActivityRepository repository;
    private final InstanceConfiguration configuration;

    @Inject
    public InstanceActivityService(final InstanceActivityRepository repository,
                                   final InstanceConfiguration configuration) {
        this.repository = repository;
        this.configuration = configuration;
    }

    public boolean cleanupActive() {
        return this.configuration.activityRetentionPeriodDays() != 0;
    }

    public void cleanup() {
        if (this.cleanupActive()) {
            this.repository.cleanup(this.configuration.activityRetentionPeriodDays());
        }
    }

    public InstanceActivity create(String userId, Long instanceId, InstanceActivityType action) {
        InstanceActivity instanceActivity = new InstanceActivity(userId, instanceId, action);
        this.repository.save(instanceActivity);

        return instanceActivity;
    }

}
