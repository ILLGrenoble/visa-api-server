package eu.ill.visa.vdi.brokers.redis.messages;

import eu.ill.visa.vdi.domain.models.Role;

public record AccessRequestResponseMessage(Long instanceId, String requesterConnectionId, Role role) {
}
