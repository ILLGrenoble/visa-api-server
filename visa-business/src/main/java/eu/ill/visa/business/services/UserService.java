package eu.ill.visa.business.services;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.UserRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class UserService {

    private final UserRepository repository;

    @Inject
    public UserService(final UserRepository repository) {
        this.repository = repository;
    }

    public User getById(final String id) {
        return repository.getById(id);
    }

    public List<User> getAll() {
        return repository.getAll();
    }

    public List<User> getAll(OrderBy orderBy, Pagination pagination) {
        return this.getAll(new QueryFilter(), orderBy, pagination);
    }

    public List<User> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<User> getAllActivated() {
        return repository.getAllActivated();
    }

    public List<User> getAllLikeLastName(@NotNull final String lastName) {
        return repository.getAllLikeLastName(lastName);
    }

    public List<User> getAllLikeLastName(@NotNull final String lastName, @NotNull final Pagination pagination) {
        return repository.getAllLikeLastName(lastName, pagination);
    }

    public Long countAllLikeLastName(@NotNull final String lastName) {
        return repository.countAllLikeLastName(lastName);
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

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }

    public Long countAllActivated() {
        return repository.countAllActivated();
    }


}
