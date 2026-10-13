package eu.ill.visa.broker.domain.exceptions;


public class BrokerExceptionFormatter {
    public static String formatException(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
    }
}
