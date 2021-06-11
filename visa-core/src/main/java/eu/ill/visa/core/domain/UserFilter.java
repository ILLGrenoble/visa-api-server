package eu.ill.visa.core.domain;

public class UserFilter {

    private String role;

    public UserFilter() {

    }

    public UserFilter(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
