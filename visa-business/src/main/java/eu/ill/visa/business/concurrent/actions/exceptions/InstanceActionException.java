package eu.ill.visa.business.concurrent.actions.exceptions;

public class InstanceActionException extends Exception {

    public InstanceActionException() {
    }

    public InstanceActionException(String message) {
        super(message);
    }

    public InstanceActionException(String message, Throwable cause) {
        super(message, cause);
    }
}

