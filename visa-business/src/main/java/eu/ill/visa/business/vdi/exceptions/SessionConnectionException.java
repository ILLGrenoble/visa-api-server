package eu.ill.visa.business.vdi.exceptions;


public class SessionConnectionException extends Exception {
    public SessionConnectionException() {
    }

    public SessionConnectionException(String message) {
        super(message);
    }

    public SessionConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
