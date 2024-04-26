package eu.ill.visa.security.authorizers;

import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.security.tokens.AccountToken;
import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ApplicationAuthorizer implements Authorizer<AccountToken> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationAuthorizer.class);

    public ApplicationAuthorizer() {
    }

    @Override
    public boolean authorize(final AccountToken principal, final String role) {
        final String login = principal.getName();
        final User user = principal.getUser();
        if (user == null) {
            logger.info("[Authorisation] User {} is not authorized to access given resource", login);
            return false;
        }
        return user.hasRole(role);
    }
}
