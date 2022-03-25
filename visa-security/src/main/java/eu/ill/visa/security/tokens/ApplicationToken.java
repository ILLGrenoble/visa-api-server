package eu.ill.visa.security.tokens;


import eu.ill.visa.core.domain.ApplicationCredential;

import java.security.Principal;

public class ApplicationToken implements Principal {

    private ApplicationCredential applicationCredential;

    public ApplicationToken() {
    }

    public ApplicationToken(final ApplicationCredential applicationCredential) {
        this.applicationCredential = applicationCredential;
    }

    @Override
    public String getName() {
        return applicationCredential.getName();
    }

    public ApplicationCredential getApplicationCredential() {
        return applicationCredential;
    }

    public void setApplicationCredential(ApplicationCredential applicationCredential) {
        this.applicationCredential = applicationCredential;
    }
}

