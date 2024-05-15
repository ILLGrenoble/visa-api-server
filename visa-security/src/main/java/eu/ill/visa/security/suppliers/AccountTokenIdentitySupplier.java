package eu.ill.visa.security.suppliers;

import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.security.authenticator.AccountTokenAuthenticator;
import eu.ill.visa.security.tokens.AccountToken;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import java.util.function.Supplier;
import java.util.stream.Collectors;

@Dependent
public class AccountTokenIdentitySupplier implements Supplier<SecurityIdentity> {

    private final AccountTokenAuthenticator authenticator;
    private TokenCredential token;

    @Inject
    AccountTokenIdentitySupplier(final AccountTokenAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setToken(TokenCredential token) {
        this.token = token;
    }

    @ActivateRequestContext
    public SecurityIdentity get() {
        if (token != null) {
            final AccountToken accountToken = this.authenticator.authenticate(this.token.getToken()).orElse(null);

            if (accountToken != null) {
                final User user = accountToken.getUser();
                // Create SecurityIdentity with AccountToken as Principal and User Roles
                return QuarkusSecurityIdentity.builder()
                    .setPrincipal(accountToken)
                    .addRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                    .setAnonymous(false)
                    .build();
            }
        }

        throw new AuthenticationFailedException("invalid token");
    }

}
