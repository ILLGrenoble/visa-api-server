package eu.ill.visa.broker.domain.messages;

public record EventForUserMessage(String userId, Object event) {
}
