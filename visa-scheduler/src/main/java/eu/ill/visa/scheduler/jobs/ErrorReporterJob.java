package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.notification.logging.filters.ErrorReporter;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ErrorReporterJob {

    private static final Logger logger = LoggerFactory.getLogger(ErrorReporterJob.class);

    private final ErrorReporter errorReporter;

    @Inject
    public ErrorReporterJob(final ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    @Scheduled(cron="5/10 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void maxErrorsScheduler() {
        this.errorReporter.handleMaxErrors();
    }

    @Scheduled(cron="0 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void minuteScheduler() {
        this.errorReporter.handleCurrentErrors();
    }
}
