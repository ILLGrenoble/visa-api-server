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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Supplier;

@Dependent
public class SimpleHeaderTokenIdentitySupplier implements Supplier<SecurityIdentity> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleHeaderTokenIdentitySupplier.class);

    public static final String X_VDI_AUTH_TOKEN_HEADER = "X-VDI-Auth-Token";

    private final String vdiAuthToken;
    private TokenCredential token;

    @Inject
    SimpleHeaderTokenIdentitySupplier(final SecurityConfiguration securityConfiguration) {
        this.vdiAuthToken = securityConfiguration.vdiAuthToken().orElseGet(() -> {
            final String token = UUID.randomUUID().toString();
            logger.info("No VDI Auth Token has been specified in the configuration. This should be set using the VISA_VDI_API_SERVER_AUTH_TOKEN environment variable if you are using the VISA VDI Microservice. For now assigning a random value: {}", token);
            return token;
        });
    }

    public void setToken(TokenCredential token) {
        this.token = token;
    }

    @ActivateRequestContext
    public SecurityIdentity get() {
        if (token != null) {
            if (token.getType().equals(X_VDI_AUTH_TOKEN_HEADER) && token.getToken().equals(this.vdiAuthToken)) {
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
