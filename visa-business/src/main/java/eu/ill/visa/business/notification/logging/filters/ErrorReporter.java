package eu.ill.visa.business.notification.logging.filters;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.jboss.logmanager.ExtLogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ErrorReporter implements Runnable {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Logger logger = LoggerFactory.getLogger(ErrorReporter.class);

    private final Mailer mailer;
    private final String subject;
    private final String toAddress;
    private final List<String> ccAddresses;
    private final String fromAddress;
    private final int maxErrorsPerReport;


    private boolean running = false;
    private Date lastReportingTime = null;
    private List<ErrorEvent> events = new ArrayList<>();


    public ErrorReporter(final Mailer mailer,
                         final String subject,
                         final String toAddress,
                         final List<String> ccAddresses,
                         final String fromAddress,
                         final int maxErrorsPerReport) {
        this.mailer = mailer;
        this.subject = subject;
        this.toAddress = toAddress;
        this.ccAddresses = ccAddresses;
        this.fromAddress = fromAddress;
        this.maxErrorsPerReport = maxErrorsPerReport;
    }

    @Override
    public void run() {
        this.running = true;

        while (running) {
            try {
                Thread.sleep(1000);
                this.work();
            } catch (InterruptedException ignored) {
            }
        }

    }

    public void stop() {
        this.running = false;
    }

    public synchronized void work() {
        Date currentTime = new Date();
        Long elapsedTime = this.lastReportingTime == null ? null : currentTime.getTime() - this.lastReportingTime.getTime();
        if ((!this.events.isEmpty() && (elapsedTime == null || elapsedTime >= 60000)) || this.events.size() >= this.maxErrorsPerReport) {

            final List<ErrorEvent> events = this.events;
            this.events = new ArrayList<>();

            Uni.createFrom()
                .item(events)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with(this::generateReport, Throwable::printStackTrace);

            this.lastReportingTime = currentTime;
        }
    }

    public synchronized void onRecord(LogRecord record) {
        this.events.add(new ErrorEvent(new Date(), record, Thread.currentThread().getStackTrace()));
    }

    private void generateReport(final List<ErrorEvent> events) {
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
