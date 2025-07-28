package eu.ill.visa.business.notification.logging.filters;

import eu.ill.visa.business.ErrorReportEmailConfiguration;
import eu.ill.visa.core.domain.Timer;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailerName;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.runtime.Shutdown;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.inject.Singleton;
import org.jboss.logmanager.ExtLogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Unremovable
@LookupIfProperty(name = "business.errorReportEmail.enabled", stringValue = "true")
@Singleton
public class ActiveErrorReporter implements ErrorReporter {

    private final static int MAX_ERRORS_WORKER_TIME_MS = 5000;
    private final static int PENDING_ERRORS_WORKER_TIME_MS = 60000;
    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Logger logger = LoggerFactory.getLogger(ActiveErrorReporter.class);

    private final ReactiveMailer mailer;
    private final String subject;
    private final String toAddress;
    private final List<String> ccAddresses;
    private final String fromAddress;
    private final int maxErrorsPerReport;

    private final boolean enabled;

    private final Cancellable maxErrorsIntervalSubscription;
    private final Cancellable pendingErrorsIntervalSubscription;

    private List<ErrorEvent> events = new ArrayList<>();

    private final Executor errorReportExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("error-reporter-vt-", 0).factory());;

    public ActiveErrorReporter(final @MailerName("logging") ReactiveMailer mailer,
                               final ErrorReportEmailConfiguration configuration) {
        this.mailer = mailer;

        List<String> addresses = configuration.to().orElse(null);
        if (addresses != null && !addresses.isEmpty()) {
            this.toAddress = addresses.removeFirst();
            this.ccAddresses = addresses;

        } else {
            this.toAddress = null;
            this.ccAddresses = new ArrayList<>();
        }

        this.fromAddress = configuration.from().orElse(null);
        this.subject = configuration.subject().orElse(null);
        this.maxErrorsPerReport = configuration.maxErrors();

        this.enabled =  (toAddress != null && fromAddress != null && subject != null);
        if (enabled) {
            logger.info("Error reporting is enabled");

            this.maxErrorsIntervalSubscription = Timer.setInterval(this::handleMaxErrors, MAX_ERRORS_WORKER_TIME_MS, TimeUnit.MILLISECONDS);
            this.pendingErrorsIntervalSubscription = Timer.setInterval(this::handlePendingErrors, PENDING_ERRORS_WORKER_TIME_MS, TimeUnit.MILLISECONDS);

        } else {
            logger.info("Error reporting is disabled (configuration is not valid)");
            maxErrorsIntervalSubscription = null;
            pendingErrorsIntervalSubscription = null;
        }
    }

    @Shutdown
    public void stop() {
        if (enabled) {
            this.maxErrorsIntervalSubscription.cancel();
            this.pendingErrorsIntervalSubscription.cancel();
        }
    }

    public synchronized void handleMaxErrors() {
        if (this.events.size() >= this.maxErrorsPerReport) {
            this.generateReportInVirtualThread(events);
        }
    }

    public synchronized void handlePendingErrors() {
        if (!this.events.isEmpty()) {
            this.generateReportInVirtualThread(events);
        }
    }

    public synchronized void onRecord(LogRecord record) {
        if (this.enabled) {
            this.events.add(new ErrorEvent(new Date(), record, Thread.currentThread().getStackTrace()));
        }
    }

    private void generateReportInVirtualThread(final List<ErrorEvent> eventsList) {
        this.events = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            try {
                this.generateReport(eventsList);
            } catch (Exception e) {
                logger.error("Error while generating error report: {}", e.getMessage());
            }
        }, errorReportExecutor);
    }

    private void generateReport(final List<ErrorEvent> events) {
        if (!this.enabled) {
            return;
        }

        logger.info("Generating error report for {} events ", events.size());

        String errors = events.stream()
            .map(event -> {
                LogRecord logRecord = event.log();
                String error;
                if (logRecord instanceof ExtLogRecord extLogRecord) {
                    error = format("%s ERROR [%s] %s", DATE_FORMAT.format(Date.from(logRecord.getInstant())), extLogRecord.getLoggerName(), logRecord.getMessage());
                } else {
                    error = format("%s ERROR [???] %s", DATE_FORMAT.format(Date.from(logRecord.getInstant())), logRecord.getMessage());
                }

                if (logRecord.getThrown() != null) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    logRecord.getThrown().printStackTrace(printWriter);
                    error = format("%s\n%s\n", error, stringWriter);
                }
                return error;
            })
            .collect(Collectors.joining("\n"));

        String subject = format("%s: %d errors", this.subject, events.size());

        String body = format("The following errors have been logged by VISA:\n\n%s", errors);

        final Mail email = Mail.withText(this.toAddress, subject, body);
        for (String cc : this.ccAddresses) {
            email.addCc(cc);
        }
        email.setFrom(this.fromAddress);
        try {
            this.mailer.send(email);

        } catch (Exception e) {
            logger.warn("Failed to send error report email: {}", e.getMessage());
        }
    }

    public record ErrorEvent(Date time, LogRecord log, StackTraceElement[] stack) {
    }
}
