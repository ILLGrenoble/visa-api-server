package eu.ill.visa.common.vdi.apiconnect.exceptions;

public class InvalidTokenException extends Exception {
    public InvalidTokenException() {
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
