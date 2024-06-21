package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.vdi.domain.models.Role;

public class AccessRequestReply {
    Long instanceId;
    String requesterConnectionId;
    String response;

    public AccessRequestReply(Long instanceId, String requesterConnectionId, String response) {
        this.instanceId = instanceId;
        this.requesterConnectionId = requesterConnectionId;
        this.response = response;
    }

    public AccessRequestReply() {
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getRequesterConnectionId() {
        return requesterConnectionId;
    }

    public void setRequesterConnectionId(String requesterConnectionId) {
        this.requesterConnectionId = requesterConnectionId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Role getRole() {
        if (this.response.equals("GUEST")) {
            return Role.GUEST;
        } else if (this.response.equals("SUPPORT")) {
            return Role.SUPPORT;
        }
        return Role.NONE;
    }
}
