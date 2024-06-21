package eu.ill.visa.vdi.brokers.messages;

import eu.ill.visa.vdi.domain.models.Role;

public record AccessRequestResponseMessage(Long instanceId, String requesterConnectionId, Role role) {
}
