package eu.ill.visa.vdi.business.services;

import eu.ill.visa.business.services.InstanceAuthenticationTokenService;
import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import eu.ill.visa.vdi.domain.exceptions.InvalidTokenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TokenAuthenticatorService {

    private final InstanceAuthenticationTokenService instanceAuthenticationTokenService;

    @Inject
    public TokenAuthenticatorService(final InstanceAuthenticationTokenService instanceAuthenticationTokenService) {
        this.instanceAuthenticationTokenService = instanceAuthenticationTokenService;
    }

    public InstanceAuthenticationToken authenticate(final String token) throws InvalidTokenException {
        if (token == null) {
            throw new InvalidTokenException("Could not find or session ticket is invalid");
        }

        final InstanceAuthenticationToken authenticationToken = instanceAuthenticationTokenService.getByToken(token);

        if (authenticationToken == null) {
            throw new InvalidTokenException("Authentication session ticket not found");
        }

        if (authenticationToken.isExpired(10)) {
            throw new InvalidTokenException("Authentication session ticket has expired");
        }

        return authenticationToken;
    }
}
