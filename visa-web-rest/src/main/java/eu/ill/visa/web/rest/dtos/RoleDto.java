package eu.ill.visa.web.rest.dtos;

import java.util.Date;

public class RoleDto {

    private String name;
    private Date expiresAt;

    public RoleDto(String name, Date expiresAt) {
        this.name = name;
        this.expiresAt = expiresAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
