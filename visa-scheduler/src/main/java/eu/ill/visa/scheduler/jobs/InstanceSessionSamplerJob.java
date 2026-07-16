package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.vdi.business.services.DesktopSessionService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class InstanceSessionSamplerJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSessionSamplerJob.class);

    private final DesktopSessionService desktopSessionService;

    @Inject
    public InstanceSessionSamplerJob(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    // Run every minute
    @Scheduled(cron="0 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        logger.info("Executing instance session sample job...");

        long count = this.desktopSessionService.storeDesktopSessionConnectionStats();

        logger.info("... stored {} samples", count);
    }
}
