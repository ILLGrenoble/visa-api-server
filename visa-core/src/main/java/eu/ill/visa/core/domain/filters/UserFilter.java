package eu.ill.visa.core.domain.filters;


import jakarta.ws.rs.QueryParam;

import java.util.List;

public class UserFilter {

    @QueryParam("id")
    private List<String> ids;

    @QueryParam("role")
    private String role;

    @QueryParam("activated")
    private Boolean activated;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
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
