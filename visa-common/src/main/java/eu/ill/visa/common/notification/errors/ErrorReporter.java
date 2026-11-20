package eu.ill.visa.common.notification.errors;

import java.util.logging.LogRecord;

public interface ErrorReporter {
    void handleMaxErrors();
    void handlePendingErrors();
    void onRecord(LogRecord record);
}
