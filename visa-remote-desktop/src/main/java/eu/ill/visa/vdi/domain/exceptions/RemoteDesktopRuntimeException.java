package eu.ill.visa.vdi.domain.exceptions;

public class RemoteDesktopRuntimeException extends RuntimeException {
    public RemoteDesktopRuntimeException() {
    }

    public RemoteDesktopRuntimeException(String message) {
        super(message);
    }

    public RemoteDesktopRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
