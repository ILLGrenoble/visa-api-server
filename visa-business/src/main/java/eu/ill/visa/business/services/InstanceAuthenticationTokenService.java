package eu.ill.visa.business.services;

import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.core.domain.fetches.InstanceFetch;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.InstanceAuthenticationTokenRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@Singleton
public class InstanceAuthenticationTokenService {

    private final InstanceAuthenticationTokenRepository repository;
    final InstanceService instanceService;

    @Inject
    public InstanceAuthenticationTokenService(final InstanceAuthenticationTokenRepository repository,
                                              final InstanceService instanceService) {
        this.repository = repository;
        this.instanceService = instanceService;
    }

    public List<InstanceAuthenticationToken> getAll() {
        return this.repository.getAll();
    }

    public InstanceAuthenticationToken getByToken(String token) {
        InstanceAuthenticationToken instanceAuthenticationToken = this.repository.getByToken(token);
        Instance instance = instanceAuthenticationToken.getInstance();
        instanceAuthenticationToken.setInstance(this.instanceService.handleFetches(instance, List.of(InstanceFetch.members)));
        return instanceAuthenticationToken;
    }

    public InstanceAuthenticationToken create(User user, Instance instance) {

        InstanceAuthenticationToken instanceAuthenticationToken = InstanceAuthenticationToken.newBuilder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .instance(instance)
            .build();

        this.save(instanceAuthenticationToken);
        return instanceAuthenticationToken;
    }

    public void save(InstanceAuthenticationToken instanceAuthenticationToken) {
        this.repository.save(instanceAuthenticationToken);
    }

    public void delete(InstanceAuthenticationToken InstanceAuthenticationToken) {
        this.repository.delete(InstanceAuthenticationToken);
    }

    public synchronized InstanceAuthenticationToken authenticate(final String token) throws InvalidTokenException {
        if (token == null) {
            throw new InvalidTokenException("Could not find or session ticket is invalid");
        }

        final InstanceAuthenticationToken authenticationToken = this.getByToken(token);

        if (authenticationToken == null) {
            throw new InvalidTokenException("Authentication session ticket not found");
        }

        if (authenticationToken.isExpired(10)) {
            throw new InvalidTokenException("Authentication session ticket has expired");
        }

        // Delete the authentication token to ensure it isn't reused
        this.delete(authenticationToken);

        return authenticationToken;
    }
}
