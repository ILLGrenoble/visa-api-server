package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.exceptions.CloudException;

public class AuthenticatedOpenStackProvider {
    private final OpenStackIdentityProvider identityProvider;

    public AuthenticatedOpenStackProvider(final OpenStackIdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public String authenticate() throws CloudException {
        return this.authenticate(false);
    }

    public String authenticate(boolean force) throws CloudException {
        return this.identityProvider.authenticate(force);
    }

}
