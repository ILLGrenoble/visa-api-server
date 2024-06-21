package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public class AccessRequest {
    private Long instanceId;
    private String requesterConnectionId;
    private ConnectedUser user;

    public AccessRequest() {
    }

    public AccessRequest(Long instanceId, ConnectedUser user, String requesterConnectionId) {
        this.instanceId = instanceId;
        this.user = user;
        this.requesterConnectionId = requesterConnectionId;
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

    public ConnectedUser getUser() {
        return user;
    }

    public void setUser(ConnectedUser user) {
        this.user = user;
    }
}
