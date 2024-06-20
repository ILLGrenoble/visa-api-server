package eu.ill.visa.vdi.domain.models;

public class AccessRequest {
    private String token;
    private ConnectedUser user;

    public AccessRequest() {
    }

    public AccessRequest(ConnectedUser user, String token) {
        this.user = user;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ConnectedUser getUser() {
        return user;
    }

    public void setUser(ConnectedUser user) {
        this.user = user;
    }
}
