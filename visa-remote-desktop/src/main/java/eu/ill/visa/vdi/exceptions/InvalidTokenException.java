package eu.ill.visa.vdi.exceptions;

public class InvalidTokenException extends RemoteDesktopException {
    public InvalidTokenException() {
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
