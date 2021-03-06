package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Role;
import eu.ill.visa.persistence.repositories.RoleRepository;

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

    public Role getByName(String name) {
        return this.repository.getByName(name);
    }
}
