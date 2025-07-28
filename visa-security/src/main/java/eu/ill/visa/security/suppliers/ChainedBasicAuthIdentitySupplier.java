package eu.ill.visa.security.suppliers;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import java.util.function.Supplier;

@Dependent
public class ChainedBasicAuthIdentitySupplier implements Supplier<SecurityIdentity> {

    private final ApplicationTokenIdentitySupplier applicationTokenIdentitySupplier;
    private final InstanceTokenIdentitySupplier instanceTokenIdentitySupplier;

    private String username;
    private String password;

    @Inject
    ChainedBasicAuthIdentitySupplier(final ApplicationTokenIdentitySupplier applicationTokenIdentitySupplier,
                                     final InstanceTokenIdentitySupplier instanceTokenIdentitySupplier) {
        this.applicationTokenIdentitySupplier = applicationTokenIdentitySupplier;
        this.instanceTokenIdentitySupplier = instanceTokenIdentitySupplier;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ActivateRequestContext
    public SecurityIdentity get() {
        SecurityIdentity identity = instanceTokenIdentitySupplier.authenticate(this.username, this.password);

        if (identity == null) {
            identity = applicationTokenIdentitySupplier.authenticate(this.username, this.password);
        }

        if (identity == null) {
            throw new AuthenticationFailedException("invalid username or password");
        }

        return identity;
    }
}
