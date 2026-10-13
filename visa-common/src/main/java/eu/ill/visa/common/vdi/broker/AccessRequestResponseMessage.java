package eu.ill.visa.common.vdi.broker;

import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRequestResponseMessage(String connectionId, String requesterClientId, InstanceMemberRole role) {
}
