package eu.ill.visa.vdi.domain.exceptions;

public class ApiCommunicationException extends Exception {
    public ApiCommunicationException() {
    }

    public ApiCommunicationException(String message) {
        super(message);
    }

    public ApiCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
