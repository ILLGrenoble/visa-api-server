package eu.ill.visa.security.suppliers;

import eu.ill.visa.business.services.ApplicationCredentialService;
import eu.ill.visa.core.entity.ApplicationCredential;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.security.tokens.ApplicationToken;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Dependent
public class ApplicationTokenIdentitySupplier {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationTokenIdentitySupplier.class);

    private final ApplicationCredentialService applicationCredentialService;

    @Inject
    ApplicationTokenIdentitySupplier(final ApplicationCredentialService applicationCredentialService) {
        this.applicationCredentialService = applicationCredentialService;
    }


    public SecurityIdentity authenticate(final String applicationId, final String applicationSecret) {
        if (applicationId != null && applicationSecret != null) {
            ApplicationCredential applicationCredential = this.applicationCredentialService.getByApplicationIdAndApplicationSecret(applicationId, applicationSecret);

            if (applicationCredential != null) {
                applicationCredential.setLastUsedAt(new Date());
                this.applicationCredentialService.save(applicationCredential);
                logger.debug("Successfully authenticated application: {}", applicationCredential.getName());

                ApplicationToken applicationToken = new ApplicationToken(applicationCredential);

                // Create SecurityIdentity with ApplicationCredential as Principal and single APPLICATION_CREDENTIAL_ROLE role
                return QuarkusSecurityIdentity.builder()
                    .setPrincipal(applicationToken)
                    .addRole(Role.APPLICATION_CREDENTIAL_ROLE)
                    .setAnonymous(false)
                    .build();
            }
        }

        return null;
    }
}
