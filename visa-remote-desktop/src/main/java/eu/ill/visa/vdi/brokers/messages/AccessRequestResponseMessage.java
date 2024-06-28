package eu.ill.visa.vdi.brokers.messages;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

public record AccessRequestResponseMessage(Long sessionId, String requesterConnectionId, InstanceMemberRole role) {
}
