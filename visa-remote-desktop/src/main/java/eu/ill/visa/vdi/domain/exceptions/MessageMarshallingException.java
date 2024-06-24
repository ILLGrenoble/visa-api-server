package eu.ill.visa.vdi.domain.exceptions;

public class MessageMarshallingException extends RemoteDesktopRuntimeException {
    public MessageMarshallingException() {
    }

    public MessageMarshallingException(String message) {
        super(message);
    }

    public MessageMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }
}
