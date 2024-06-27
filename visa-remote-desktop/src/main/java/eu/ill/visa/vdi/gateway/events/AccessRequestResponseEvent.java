package eu.ill.visa.vdi.gateway.events;


import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

public record AccessRequestResponseEvent(Long instanceId, String protocol, String requesterConnectionId, String response) {
    public InstanceMemberRole getRole() {
        if (this.response.equals("GUEST")) {
            return InstanceMemberRole.GUEST;
        } else if (this.response.equals("SUPPORT")) {
            return InstanceMemberRole.SUPPORT;
        }
        return InstanceMemberRole.NONE;
    }
}
