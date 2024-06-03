package eu.ill.visa.cloud.exceptions;

public class CloudNotFoundException extends RuntimeException {

    public CloudNotFoundException() {
    }

    public CloudNotFoundException(String message) {
        super(message);
    }

    public CloudNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

