package eu.ill.visa.broker.domain.models;

public record ClientEventMessage(String clientId, Object event) {
}
