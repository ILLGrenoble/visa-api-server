package eu.ill.visa.business.notification.logging.filters;

import eu.ill.visa.business.ErrorReportEmailConfiguration;
import io.quarkus.logging.LoggingFilter;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.MailerName;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.spi.CDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

@LoggingFilter(name = "error-email-filter")
public class ErrorLogReportFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogReportFilter.class);

    private ErrorReporter errorReporter;
    private Thread reporterThread;

    @Startup
    public void start() {
        try {
            Mailer mailer = CDI.current().select(Mailer.class, MailerName.Literal.of("logging")).get();
            ErrorReportEmailConfiguration configuration = CDI.current().select(ErrorReportEmailConfiguration.class).get();
            List<String> addresses = configuration.to().orElse(null);
            String toAddress = null;
            List<String> ccAddresses = null;
            if (addresses != null && !addresses.isEmpty()) {
                toAddress = addresses.removeFirst();
                ccAddresses = addresses;
            }
            String fromAddress = configuration.from().orElse(null);
            String subject = configuration.subject().orElse(null);
            int maxErrorsPerReport = configuration.maxErrors();

            boolean enabled =  (toAddress != null && fromAddress != null && subject != null && configuration.enabled());

            if (enabled) {
                logger.info("Starting Error Reporter");
                this.errorReporter = new ErrorReporter(mailer, subject, toAddress, ccAddresses, fromAddress, maxErrorsPerReport);
                this.reporterThread = new Thread(this.errorReporter);
                this.reporterThread.start();
            }
        } catch (Exception e) {
            logger.warn("Failed to start Error Reporter: {}", e.getMessage());
        }
    }

    @Shutdown
    public void stop() {
        if (this.errorReporter != null) {
            this.errorReporter.stop();
            this.errorReporter = null;
            try {
                logger.info("Stopping Error Reporter");
                this.reporterThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record.getLevel().equals(Level.SEVERE) && this.errorReporter != null) {
            this.errorReporter.onRecord(record);
        }
        return true;
    }

}
