package eu.ill.visa.vdi.broker;

public record AccessRevokedMessage(Long sessionId, String userId) {
}
