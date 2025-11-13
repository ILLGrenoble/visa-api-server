package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.services.CloudResourcesService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class CloudResourceUpdateJob {

    private static final Logger logger = LoggerFactory.getLogger(CloudResourceUpdateJob.class);

    private final CloudResourcesService cloudResourcesService;

    @Inject
    public CloudResourceUpdateJob(final CloudResourcesService cloudResourcesService) {
        this.cloudResourcesService = cloudResourcesService;
    }

    // Update usage every minute
    @Scheduled(cron="15 0/1 * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void updateUsage() {
        this.cloudResourcesService.updateCloudResources();
    }
}
