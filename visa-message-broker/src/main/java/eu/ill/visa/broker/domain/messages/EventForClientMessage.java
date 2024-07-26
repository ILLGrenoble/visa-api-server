package eu.ill.visa.broker.domain.messages;

public record EventForClientMessage(String clientId, Object event) {
}
