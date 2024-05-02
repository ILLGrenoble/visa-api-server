package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.services.InstanceActivityService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class InstanceActivityCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceActivityCleanupJob.class);

    private final InstanceActivityService instanceActivityService;

    @Inject
    public InstanceActivityCleanupJob(final InstanceActivityService instanceActivityService) {
        this.instanceActivityService = instanceActivityService;
    }

    // Run every day at 1am
    @Scheduled(cron="0 0 1 ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {

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
