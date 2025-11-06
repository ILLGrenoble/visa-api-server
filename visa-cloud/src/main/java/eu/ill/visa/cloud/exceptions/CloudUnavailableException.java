package eu.ill.visa.cloud.exceptions;

public class CloudUnavailableException extends Exception {

    public CloudUnavailableException() {
    }

    public CloudUnavailableException(String message) {
        super(message);
    }

    public CloudUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

