package eu.ill.visa.web.graphql.types;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("Message")
public class Message {

    private final @NotNull String message;

    public Message(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
