package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.vdi.domain.models.Role;

public record AccessRequestReply(Long instanceId, String requesterConnectionId, String response) {
    public Role getRole() {
        if (this.response.equals("GUEST")) {
            return Role.GUEST;
        } else if (this.response.equals("SUPPORT")) {
            return Role.SUPPORT;
        }
        return Role.NONE;
    }
}
