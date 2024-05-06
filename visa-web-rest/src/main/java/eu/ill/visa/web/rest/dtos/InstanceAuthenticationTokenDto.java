package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceAuthenticationToken;

public class InstanceAuthenticationTokenDto {

    private String token;

    public InstanceAuthenticationTokenDto(final InstanceAuthenticationToken instanceAuthenticationToken) {
        this.token = instanceAuthenticationToken.getToken();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
