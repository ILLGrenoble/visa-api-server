package eu.ill.visa.vdi.scheduler;

import eu.ill.visa.vdi.business.services.DesktopSessionService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CleanupInstanceSessionsJob {

    private static final Logger logger = LoggerFactory.getLogger(CleanupInstanceSessionsJob.class);

    private final DesktopSessionService desktopSessionService;

    @Inject
    public CleanupInstanceSessionsJob(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    // Run every minute
    @Scheduled(cron="30 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        logger.debug("Executing desktop session cleanup job...");

        long count = this.desktopSessionService.cleanupStaleDesktopSessions();
        if (count == 0) {
            logger.debug("... no stale desktop sessions");

        } else {
            logger.debug("... Removed {} stale sessions", count);
        }
    }
}
