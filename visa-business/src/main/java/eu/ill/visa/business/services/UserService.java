package eu.ill.visa.business.services;


import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.filters.UserFilter;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.UserRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class UserService {

    private final UserRepository repository;
    private final InstanceConfiguration configuration;

    @Inject
    public UserService(final UserRepository repository,
                       final InstanceConfiguration configuration) {
        this.repository = repository;
        this.configuration = configuration;
    }

    public User getById(final String id) {
        return repository.getById(id);
    }

    public List<User> getAll(OrderBy orderBy, Pagination pagination) {
        return this.getAll(new UserFilter(), orderBy, pagination);
    }

    public List<User> getAll(UserFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<User> getAllLikeLastName(@NotNull final String lastName, boolean onlyActivatedUsers) {
        return repository.getAllLikeLastName(lastName, onlyActivatedUsers);
    }

    public List<User> getAllLikeLastName(@NotNull final String lastName, boolean onlyActivatedUsers, @NotNull final Pagination pagination) {
        return repository.getAllLikeLastName(lastName, onlyActivatedUsers, pagination);
    }

    public Long countAllLikeLastName(@NotNull final String lastName, boolean onlyActivatedUsers) {
        return repository.countAllLikeLastName(lastName, onlyActivatedUsers);
    }

    public List<User> getAllStaff() {
        return repository.getAllStaff();
    }

    public List<User> getAllSupport() {
        return repository.getAllSupport();
    }

    public List<User> getExperimentalTeamForInstance(@NotNull final Instance instance) {
        return repository.getExperimentalTeamForInstance(instance);
    }

    public Long countAllUsersForRole(@NotNull final String role) {
        return repository.countAllUsersForRole(role);
    }

    public void save(@NotNull final User user) {
        repository.save(user);
    }

    public Long countAll() {
        return repository.countAll();
    }

    public Long countAll(UserFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, UserFilter::new));
    }

    public Long countAllActivated() {
        return repository.countAllActivated();
    }

    public int getDefaultInstanceQuota() {
        return this.configuration.defaultUserInstanceQuota();
    }


}
