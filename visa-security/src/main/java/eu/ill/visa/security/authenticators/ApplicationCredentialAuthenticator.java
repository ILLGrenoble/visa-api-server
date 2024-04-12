package eu.ill.visa.security.authenticators;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import eu.ill.visa.business.services.ApplicationCredentialService;
import eu.ill.visa.core.domain.ApplicationCredential;
import eu.ill.visa.security.tokens.ApplicationToken;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

@Singleton
public class ApplicationCredentialAuthenticator implements Authenticator<BasicCredentials, ApplicationToken> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationCredentialAuthenticator.class);

    private final ApplicationCredentialService applicationCredentialService;

    @Inject
    public ApplicationCredentialAuthenticator(final ApplicationCredentialService applicationCredentialService) {
        this.applicationCredentialService = applicationCredentialService;
    }


    @Override
    public Optional<ApplicationToken> authenticate(BasicCredentials credentials) throws AuthenticationException {
        logger.debug("[ApplicationCredentials] Authenticating from application credentials");

        String applicationId = credentials.getUsername();
        String applicationSecret = credentials.getPassword();

        ApplicationCredential applicationCredential = this.applicationCredentialService.getByApplicationIdAndApplicationSecret(applicationId, applicationSecret);

        if (applicationCredential != null) {
            applicationCredential.setLastUsedAt(new Date());
            this.applicationCredentialService.save(applicationCredential);
            logger.info("[ApplicationCredentials] Successfully authenticated application: {}", applicationCredential.getName());

            ApplicationToken applicationToken = new ApplicationToken(applicationCredential);

            return Optional.of(applicationToken);
        }

        return Optional.empty();
    }
}
