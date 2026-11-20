package eu.ill.visa.common.notification.errors;

import io.quarkus.arc.Unremovable;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogRecord;

@Unremovable
@LookupIfProperty(name = "common.errorReportEmail.enabled", stringValue = "false")
@Singleton
public class DummyErrorReporter implements ErrorReporter {

    private static final Logger logger = LoggerFactory.getLogger(DummyErrorReporter.class);

    public DummyErrorReporter() {
        logger.info("Error reporting is disabled");
    }

    public void handleMaxErrors() {
    }

    public void handlePendingErrors() {
    }

    public void onRecord(LogRecord record) {
    }
}
