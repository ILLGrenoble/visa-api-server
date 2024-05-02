package eu.ill.visa.scheduler.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class InstanceLifecycleJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceLifecycleJob.class);

    private final InstanceExpirationService instanceExpirationService;

    @Inject
    public InstanceLifecycleJob(InstanceExpirationService instanceExpirationService) {
        this.instanceExpirationService = instanceExpirationService;
    }

    // Run every 5 minutes
    @Scheduled(cron="0 */5 * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        logger.info("Executing instance lifecycle job");

        // Find instances that are active but have been scheduled to be deleted due to inactivity
        this.instanceExpirationService.removeExpirationForAllActiveInstances();

        // Find instances that are inactive and should be scheduled to be deleted
        this.instanceExpirationService.createExpirationForAllInactiveInstances();

        // Find instances that should be scheduled to be deleted due to reaching their terminationDate
        this.instanceExpirationService.createExpirationForAllTerminatingInstances();

        // (Soft) Delete instances that have reached their expiration date
        this.instanceExpirationService.deleteAllExpired();
    }
}
