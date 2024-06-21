package eu.ill.visa.vdi.gateway.events;

public class AccessRevokedCommand {
    String userId;

    public AccessRevokedCommand() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
