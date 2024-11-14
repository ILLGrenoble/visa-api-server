package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.notification.logging.filters.ErrorReporter;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ErrorReporterJob {

    private final ErrorReporter errorReporter;

    @Inject
    public ErrorReporterJob(final jakarta.enterprise.inject.Instance<ErrorReporter> errorReporter) {
        this.errorReporter = errorReporter.get();
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
