package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Role;
import eu.ill.visa.persistence.repositories.RoleRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

@Transactional
@Singleton
public class RoleService {

    private RoleRepository repository;

    @Inject
    public RoleService(RoleRepository repository) {
        this.repository = repository;

        // Initialise data if empty
        if (this.getAll().size() == 0) {
            this.repository.initialise();
        }
    }

    public List<Role> getAll() {
        return this.repository.getAll();
    }

    public Role getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(Role role) {
        this.repository.delete(role);
    }

    public void create(@NotNull Role role) {
        this.repository.save(role);
    }
}
