package eu.ill.visa.security.providers;

import eu.ill.visa.security.suppliers.ApplicationTokenIdentitySupplier;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ApplicationTokenIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationTokenIdentityProvider.class);

    private final ApplicationTokenIdentitySupplier applicationTokenIdentitySupplier;

    public ApplicationTokenIdentityProvider(final ApplicationTokenIdentitySupplier applicationTokenIdentitySupplier) {
        this.applicationTokenIdentitySupplier = applicationTokenIdentitySupplier;
    }

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request, AuthenticationRequestContext context) {
        logger.debug("Authenticating from application credentials");

        String applicationId = request.getUsername();
        String applicationSecret = new String(request.getPassword().getPassword());

        this.applicationTokenIdentitySupplier.setApplicationId(applicationId);
        this.applicationTokenIdentitySupplier.setApplicationSecret(applicationSecret);
        return context.runBlocking(applicationTokenIdentitySupplier);
    }
}
