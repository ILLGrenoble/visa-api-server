package eu.ill.visa.security.exceptions;


public class UnauthorizedRuntimeException  extends RuntimeException {
    public UnauthorizedRuntimeException(String message) {
        super(message);
    }
}
