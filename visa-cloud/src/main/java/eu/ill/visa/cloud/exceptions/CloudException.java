package eu.ill.visa.cloud.exceptions;

public class CloudException extends Exception {

    public CloudException() {
    }

    public CloudException(String message) {
        super(message);
    }

    public CloudException(String message, Throwable cause) {
        super(message, cause);
    }
}

