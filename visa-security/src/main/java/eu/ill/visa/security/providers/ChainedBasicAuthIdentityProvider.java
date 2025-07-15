package eu.ill.visa.security.providers;

import eu.ill.visa.security.suppliers.ChainedBasicAuthIdentitySupplier;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChainedBasicAuthIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    private final ChainedBasicAuthIdentitySupplier chainedBasicAuthIdentitySupplier;

    public ChainedBasicAuthIdentityProvider(final ChainedBasicAuthIdentitySupplier chainedBasicAuthIdentitySupplier) {
        this.chainedBasicAuthIdentitySupplier = chainedBasicAuthIdentitySupplier;
    }

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request, AuthenticationRequestContext context) {
        String username = request.getUsername();
        String password = new String(request.getPassword().getPassword());

        this.chainedBasicAuthIdentitySupplier.setUsername(username);
        this.chainedBasicAuthIdentitySupplier.setPassword(password);
        return context.runBlocking(chainedBasicAuthIdentitySupplier);
    }
}
