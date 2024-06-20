package eu.ill.visa.vdi.domain.models;

public class AccessRequest {
    private String token;
    private String userFullName;

    public AccessRequest() {
    }

    public AccessRequest(String userFullName, String token) {
        this.userFullName = userFullName;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
}
