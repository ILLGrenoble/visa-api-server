package eu.ill.visa.security.providers;

import eu.ill.visa.security.authentication.AccountTokenAuthenticationRequest;
import eu.ill.visa.security.suppliers.AccountTokenIdentitySupplier;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AccountTokenIdentityProvider implements IdentityProvider<AccountTokenAuthenticationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(AccountTokenIdentityProvider.class);

    private final AccountTokenIdentitySupplier accountTokenIdentitySupplier;

    public AccountTokenIdentityProvider(final AccountTokenIdentitySupplier accountTokenIdentitySupplier) {
        this.accountTokenIdentitySupplier = accountTokenIdentitySupplier;
    }

    @Override
    public Class<AccountTokenAuthenticationRequest> getRequestType() {
        return AccountTokenAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(AccountTokenAuthenticationRequest request, AuthenticationRequestContext context) {
        logger.debug("[AccountToken] Authenticating from account token credentials");

        this.accountTokenIdentitySupplier.setToken(request.getToken());
        return context.runBlocking(accountTokenIdentitySupplier);
    }
}
