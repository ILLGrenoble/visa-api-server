package eu.ill.visa.security.providers;

import eu.ill.visa.security.authentication.SimpleHeaderTokenAuthenticationRequest;
import eu.ill.visa.security.suppliers.SimpleHeaderTokenIdentitySupplier;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SimpleHeaderTokenIdentityProvider implements IdentityProvider<SimpleHeaderTokenAuthenticationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHeaderTokenIdentityProvider.class);

    private final SimpleHeaderTokenIdentitySupplier simpleHeaderTokenIdentitySupplier;

    public SimpleHeaderTokenIdentityProvider(final SimpleHeaderTokenIdentitySupplier simpleHeaderTokenIdentitySupplier) {
        this.simpleHeaderTokenIdentitySupplier = simpleHeaderTokenIdentitySupplier;
    }

    @Override
    public Class<SimpleHeaderTokenAuthenticationRequest> getRequestType() {
        return SimpleHeaderTokenAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(SimpleHeaderTokenAuthenticationRequest request, AuthenticationRequestContext context) {
        logger.debug("[AccountToken] Authenticating from simple header token credentials");

        this.simpleHeaderTokenIdentitySupplier.setToken(request.getToken());
        return context.runBlocking(simpleHeaderTokenIdentitySupplier);
    }
}
