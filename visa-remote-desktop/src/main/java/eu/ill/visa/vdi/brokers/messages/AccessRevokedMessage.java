package eu.ill.visa.vdi.brokers.messages;

public record AccessRevokedMessage(Long sessionId, String userId) {
}
