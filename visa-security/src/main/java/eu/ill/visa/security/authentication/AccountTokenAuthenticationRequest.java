package eu.ill.visa.security.authentication;

import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.BaseAuthenticationRequest;

public class AccountTokenAuthenticationRequest extends BaseAuthenticationRequest implements AuthenticationRequest {

    private final TokenCredential token;

    public AccountTokenAuthenticationRequest(final TokenCredential token) {
        this.token = token;
    }

    public TokenCredential getToken() {
        return token;
    }
}
