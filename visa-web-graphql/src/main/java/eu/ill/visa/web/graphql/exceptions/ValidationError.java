package eu.ill.visa.web.graphql.exceptions;

public class ValidationError {

    private final String name;
    private final String message;

    public ValidationError(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}
