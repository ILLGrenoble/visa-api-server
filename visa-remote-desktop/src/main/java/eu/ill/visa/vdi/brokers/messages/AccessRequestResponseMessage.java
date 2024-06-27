package eu.ill.visa.vdi.brokers.messages;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

public record AccessRequestResponseMessage(Long instanceId, String protocol, String requesterConnectionId, InstanceMemberRole role) {
}
