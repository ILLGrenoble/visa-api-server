package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.PersonalAccessToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.persistence.repositories.PersonalAccessTokenRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Transactional
@Singleton
public class PersonalAccessTokenService {

    private final PersonalAccessTokenRepository repository;
    private final InstanceService instanceService;

    @Inject
    public PersonalAccessTokenService(final PersonalAccessTokenRepository repository,
                                      final InstanceService instanceService) {
        this.repository = repository;
        this.instanceService = instanceService;
    }

    public PersonalAccessToken create(final Instance instance, final String name, final InstanceMemberRole role) {
        PersonalAccessToken personalAccessToken = PersonalAccessToken.newBuilder()
            .instance(instance)
            .name(name)
            .role(role)
            .token(UUID.randomUUID().toString())
            .build();

        this.save(personalAccessToken);
        return personalAccessToken;
    }

    public PersonalAccessToken getByInstanceAndId(final Instance instance, final Long id) {
        return this.repository.getByInstanceAndId(instance, id);
    }

    public PersonalAccessToken getByInstanceAndToken(final Instance instance, final String token) {
        return this.repository.getByInstanceAndToken(instance, token);
    }

    public List<PersonalAccessToken> getAllForInstance(final Instance instance) {
        return this.repository.getAllForInstance(instance);
    }

    public Instance consume(final PersonalAccessToken personalAccessToken, final User user) {

        InstanceMember instanceMember = InstanceMember.newBuilder()
            .user(user)
            .role(personalAccessToken.getRole())
            .build();


        final Instance instance = this.instanceService.getById(personalAccessToken.getInstance().getId());
        instance.addMember(instanceMember);

        this.instanceService.save(instance);

        personalAccessToken.setUser(user);
        personalAccessToken.setActivatedAt(new Date());
        this.save(personalAccessToken);

        return instance;
    }

    public PersonalAccessToken save(PersonalAccessToken personalAccessToken) {
        this.repository.save(personalAccessToken);
        return personalAccessToken;
    }

    public void delete(final PersonalAccessToken personalAccessToken) {
        personalAccessToken.setDeletedAt(new Date());
        this.save(personalAccessToken);
    }
}
