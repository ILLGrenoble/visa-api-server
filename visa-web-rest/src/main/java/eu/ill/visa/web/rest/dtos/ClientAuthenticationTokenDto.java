package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.ClientAuthenticationToken;

public class ClientAuthenticationTokenDto {

    private String token;

    public ClientAuthenticationTokenDto(final ClientAuthenticationToken clientAuthenticationToken) {
        this.token = clientAuthenticationToken.getToken();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
