package eu.ill.visa.common.utils.exceptions;


public class ExceptionFormatter {
    public static String formatException(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
    }
}
