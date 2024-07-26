package eu.ill.visa.broker.domain.exceptions;

public class MessageMarshallingException extends MessageBrokerRuntimeException {
    public MessageMarshallingException() {
    }

    public MessageMarshallingException(String message) {
        super(message);
    }

    public MessageMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }
}
