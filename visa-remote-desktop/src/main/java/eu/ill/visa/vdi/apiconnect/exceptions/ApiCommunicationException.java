package eu.ill.visa.vdi.apiconnect.exceptions;

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
