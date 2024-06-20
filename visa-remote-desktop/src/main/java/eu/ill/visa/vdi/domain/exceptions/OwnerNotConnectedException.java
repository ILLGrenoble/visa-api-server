package eu.ill.visa.vdi.domain.exceptions;

public class OwnerNotConnectedException extends RemoteDesktopException {
    public OwnerNotConnectedException() {
    }

    public OwnerNotConnectedException(String message) {
        super(message);
    }

    public OwnerNotConnectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
