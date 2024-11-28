package eu.ill.visa.core.domain.filters;


import jakarta.ws.rs.QueryParam;

public class UserFilter {

    @QueryParam("id")
    private String id;

    @QueryParam("role")
    private String role;

    @QueryParam("activated")
    private Boolean activated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }
}
