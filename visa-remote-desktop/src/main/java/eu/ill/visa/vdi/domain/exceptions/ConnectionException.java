package eu.ill.visa.vdi.domain.exceptions;

public class ConnectionException extends RemoteDesktopException {
    public ConnectionException() {
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
