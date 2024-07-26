package eu.ill.visa.broker.domain.models;

public record UserEventMessage(String userId, Object event) {
}
