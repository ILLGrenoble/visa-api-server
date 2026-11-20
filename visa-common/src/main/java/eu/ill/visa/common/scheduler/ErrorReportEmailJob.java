package eu.ill.visa.common.scheduler;


import eu.ill.visa.common.notification.errors.ErrorReporter;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ErrorReportEmailJob {

    private static final Logger logger = LoggerFactory.getLogger(ErrorReportEmailJob.class);

    private final ErrorReporter errorReporter;

    public ErrorReportEmailJob(final jakarta.enterprise.inject.Instance<ErrorReporter> errorReporterInstance) {
        this.errorReporter = errorReporterInstance.get();
    }

    @Scheduled(cron="0/5 * * * * ?",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void maxErrorsReportEmail() {
        logger.debug("Executing max errors report email job...");
        this.errorReporter.handleMaxErrors();
    }

    @Scheduled(cron="0 * * * * ?",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void pendingErrorsReportEmail() {
        logger.debug("Executing pending errors report email job...");
        this.errorReporter.handlePendingErrors();
    }

}
