package eu.ill.visa.cloud.exceptions;

public class CloudClientException extends RuntimeException {

    public CloudClientException() {
    }

    public CloudClientException(String message) {
        super(message);
    }

    public CloudClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

