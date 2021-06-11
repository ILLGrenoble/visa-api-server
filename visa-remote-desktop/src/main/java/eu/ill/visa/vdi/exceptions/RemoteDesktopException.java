package eu.ill.visa.vdi.exceptions;

public class RemoteDesktopException extends Exception {
    public RemoteDesktopException() {
    }

    public RemoteDesktopException(String message) {
        super(message);
    }

    public RemoteDesktopException(String message, Throwable cause) {
        super(message, cause);
    }
}
