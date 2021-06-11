package eu.ill.visa.scheduler.jobs;

import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceExpirationService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class InstanceLifecycleJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InstanceLifecycleJob.class);

    private InstanceExpirationService instanceExpirationService;

    @Inject
    public InstanceLifecycleJob(InstanceExpirationService instanceExpirationService) {
        this.instanceExpirationService = instanceExpirationService;
    }

    @Override
    public void execute(final JobExecutionContext context) {
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
