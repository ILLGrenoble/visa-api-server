package eu.ill.visa.business.services;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Role;
import eu.ill.visa.persistence.repositories.RoleRepository;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Transactional
@Singleton
public class RoleService {

    private RoleRepository repository;

    @Inject
    public RoleService(RoleRepository repository) {
        this.repository = repository;

        // Initialise data if empty
        if (this.getAllRoles().size() == 0) {
            this.repository.initialise();
        }
    }

    public List<Role> getAllRolesAndGroups() {
        return this.repository.getAll();
    }

    public List<Role> getAllRoles() {
        return this.repository.getAllRoles();
    }

    public List<Role> getAllGroups() {
        return this.repository.getAllGroups();
    }

    public Role getById(Long id) {
        return this.repository.getById(id);
    }

    public Role getByName(String name) {
        return this.repository.getByName(name);
    }

    public void save(@NotNull final Role role) {
        repository.save(role);
    }}
