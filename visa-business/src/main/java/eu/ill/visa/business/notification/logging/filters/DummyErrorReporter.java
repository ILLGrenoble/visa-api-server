package eu.ill.visa.business.notification.logging.filters;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogRecord;

@LookupIfProperty(name = "business.errorReportEmail.enabled", stringValue = "false")
@Singleton
public class DummyErrorReporter implements ErrorReporter {

    private static final Logger logger = LoggerFactory.getLogger(DummyErrorReporter.class);

    public DummyErrorReporter() {
        logger.info("Error reporting is disabled");
    }

    public void handleMaxErrors() {
    }

    public void handleCurrentErrors() {
    }

    public void onRecord(LogRecord record) {
    }
}
