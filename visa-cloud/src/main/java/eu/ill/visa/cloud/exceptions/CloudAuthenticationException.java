package eu.ill.visa.cloud.exceptions;

public class CloudAuthenticationException extends CloudRuntimeException {

    public CloudAuthenticationException() {
    }

    public CloudAuthenticationException(String message) {
        super(message);
    }

    public CloudAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

