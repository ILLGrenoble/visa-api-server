package eu.ill.visa.business.notification.logging.filters;

import java.util.logging.LogRecord;

public interface ErrorReporter {
    void handleMaxErrors();
    void handleCurrentErrors();
    void onRecord(LogRecord record);
}
