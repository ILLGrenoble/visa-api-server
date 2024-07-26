package eu.ill.visa.vdi.broker;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

public record AccessRequestResponseMessage(Long sessionId, String requesterConnectionId, InstanceMemberRole role) {
}
