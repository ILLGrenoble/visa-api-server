package eu.ill.visa.cloud.exceptions;

public class CloudNotFoundException extends CloudRuntimeException {

    public CloudNotFoundException() {
    }

    public CloudNotFoundException(String message) {
        super(message);
    }

    public CloudNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

