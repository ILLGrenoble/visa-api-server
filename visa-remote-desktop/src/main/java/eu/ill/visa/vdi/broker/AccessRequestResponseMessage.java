package eu.ill.visa.vdi.broker;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRequestResponseMessage(Long sessionId, String requesterClientId, InstanceMemberRole role) {
}
