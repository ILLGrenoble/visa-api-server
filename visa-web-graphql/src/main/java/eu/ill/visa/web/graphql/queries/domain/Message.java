package eu.ill.visa.web.graphql.queries.domain;

public class Message {

    private String message;

    Message(String message) {
        this.message = message;
    }

    public static Message createMessage(String message) {
        return new Message(message);
    }

    public String getMessage() {
        return message;
    }
}
