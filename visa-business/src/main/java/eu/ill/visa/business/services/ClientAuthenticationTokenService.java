package eu.ill.visa.business.services;

import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.core.entity.ClientAuthenticationToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.ClientAuthenticationTokenRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@Singleton
public class ClientAuthenticationTokenService {

    private final ClientAuthenticationTokenRepository repository;

    @Inject
    public ClientAuthenticationTokenService(ClientAuthenticationTokenRepository repository) {
        this.repository = repository;
    }

    public List<ClientAuthenticationToken> getAll() {
        return this.repository.getAll();
    }

    public ClientAuthenticationToken getByTokenAndClientId(final String token, final String clientId) {
        return this.repository.getByTokenAndClientId(token, clientId);
    }

    public ClientAuthenticationToken create(final User user, final String clientId) {

        ClientAuthenticationToken clientAuthenticationToken = ClientAuthenticationToken.newBuilder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .clientId(clientId)
            .build();

        this.save(clientAuthenticationToken);
        return clientAuthenticationToken;
    }

    public void save(ClientAuthenticationToken clientAuthenticationToken) {
        this.repository.save(clientAuthenticationToken);
    }

    public void delete(ClientAuthenticationToken clientAuthenticationToken) {
        this.repository.delete(clientAuthenticationToken);
    }

    public synchronized ClientAuthenticationToken authenticate(final String token, final String clientId) throws InvalidTokenException {
        if (token == null) {
            throw new InvalidTokenException("Could not find or session ticket is invalid");
        }

        final ClientAuthenticationToken authenticationToken = this.getByTokenAndClientId(token, clientId);

        if (authenticationToken == null) {
            throw new InvalidTokenException("Client authentication token not found");
        }

        if (authenticationToken.isExpired(10)) {
            throw new InvalidTokenException("Client authentication token has expired");
        }

        // Delete the authentication token to ensure it isn't reused
        this.delete(authenticationToken);

        return authenticationToken;
    }
}
