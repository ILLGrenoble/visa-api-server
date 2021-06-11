package eu.ill.visa.web.dtos;

import eu.ill.visa.core.domain.InstanceAuthenticationToken;

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
