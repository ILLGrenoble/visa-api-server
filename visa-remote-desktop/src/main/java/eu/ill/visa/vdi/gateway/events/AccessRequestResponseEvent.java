package eu.ill.visa.vdi.gateway.events;


import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRequestResponseEvent(Long sessionId, String requesterConnectionId, String response) {
    public InstanceMemberRole getRole() {
        if (this.response.equals("GUEST")) {
            return InstanceMemberRole.GUEST;
        } else if (this.response.equals("SUPPORT")) {
            return InstanceMemberRole.SUPPORT;
        }
        return InstanceMemberRole.NONE;
    }
}
