package eu.ill.visa.security.suppliers;

import eu.ill.visa.core.entity.Role;
import eu.ill.visa.security.SecurityConfiguration;
import eu.ill.visa.security.tokens.VdiAccessToken;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import java.util.function.Supplier;

@Dependent
public class SimpleHeaderTokenIdentitySupplier implements Supplier<SecurityIdentity> {

    public static final String X_VDI_AUTH_TOKEN_HEADER = "X-VDI-Auth-Token";

    private final SecurityConfiguration securityConfiguration;
    private TokenCredential token;

    @Inject
    SimpleHeaderTokenIdentitySupplier(final SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    public void setToken(TokenCredential token) {
        this.token = token;
    }

    @ActivateRequestContext
    public SecurityIdentity get() {
        if (token != null) {
            if (token.getType().equals(X_VDI_AUTH_TOKEN_HEADER) && token.getToken().equals(this.securityConfiguration.vdiAuthToken())) {
                final VdiAccessToken accessToken = new VdiAccessToken();

                return QuarkusSecurityIdentity.builder()
                    .setPrincipal(accessToken)
                    .addRole(Role.VDI_ACCESS_ROLE)
                    .setAnonymous(false)
                    .build();
            }
        }

        throw new AuthenticationFailedException("invalid token");
    }

}
