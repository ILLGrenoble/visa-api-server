package eu.ill.visa.business.notification.logging.filters;

import io.quarkus.logging.LoggingFilter;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.spi.CDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

@LoggingFilter(name = "error-email-filter")
public class ErrorLogReportFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogReportFilter.class);

    private ErrorReporter errorReporter = null;

    @Startup
    public void start() {
        try {
            this.errorReporter = CDI.current().select(ErrorReporter.class).get();

        } catch (Exception e) {
            logger.warn("Failed to get Error Reporter: {}", e.getMessage());
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
