package eu.ill.visa.vdi.gateway.events;

public class AccessCancellation {
    private String requesterConnectionId;
    private String userFullName;

    public AccessCancellation() {
    }

    public AccessCancellation(String userFullName, String requesterConnectionId) {
        this.userFullName = userFullName;
        this.requesterConnectionId = requesterConnectionId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getRequesterConnectionId() {
        return requesterConnectionId;
    }

    public void setRequesterConnectionId(String requesterConnectionId) {
        this.requesterConnectionId = requesterConnectionId;
    }
}
