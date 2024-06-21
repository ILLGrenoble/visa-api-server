package eu.ill.visa.vdi.brokers.redis.messages;

public record AccessRevokedMessage(Long instanceId, String userId) {
}
