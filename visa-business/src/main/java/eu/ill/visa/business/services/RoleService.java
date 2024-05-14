package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Role;
import eu.ill.visa.persistence.repositories.RoleRepository;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class RoleService {

    private final RoleRepository repository;

    @Inject
    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    @Startup
    public void initRoles() {
        // Initialise data if empty
        if (this.getAllRoles().isEmpty()) {
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
