package eu.ill.visa.broker.domain.exceptions;

public class MessageBrokerRuntimeException extends RuntimeException {
    public MessageBrokerRuntimeException() {
    }

    public MessageBrokerRuntimeException(String message) {
        super(message);
    }

    public MessageBrokerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
