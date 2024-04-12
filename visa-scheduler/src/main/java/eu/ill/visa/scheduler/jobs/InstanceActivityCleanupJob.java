package eu.ill.visa.scheduler.jobs;

import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceActivityService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class InstanceActivityCleanupJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InstanceActivityCleanupJob.class);

    private final InstanceActivityService instanceActivityService;

    @Inject
    public InstanceActivityCleanupJob(final InstanceActivityService instanceActivityService) {
        this.instanceActivityService = instanceActivityService;
    }

    @Override
    public void execute(final JobExecutionContext context) {

        try {
            if (this.instanceActivityService.cleanupActive()) {
                logger.info("Executing instance activity cleanup job");
                this.instanceActivityService.cleanup();
            }

        } catch (Exception e) {
            logger.error("Failed to perform instance activity cleanup job: {}", e.getMessage());
        }
    }
}
