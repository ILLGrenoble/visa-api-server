package eu.ill.visa.cloud.exceptions;

public class CloudRuntimeException extends RuntimeException {

    public CloudRuntimeException() {
    }

    public CloudRuntimeException(String message) {
        super(message);
    }

    public CloudRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

