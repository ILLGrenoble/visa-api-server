package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Role;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class RoleRepository extends AbstractRepository<Role> {

    private static final String FIXTURES_FILE = "fixtures/roles.sql";

    @Inject
    RoleRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public void initialise() {
        this.initialiseData(FIXTURES_FILE);
    }

    public List<Role> getAll() {
        final TypedQuery<Role> query = getEntityManager().createNamedQuery("role.getAll", Role.class);
        return query.getResultList();
    }

    public List<Role> getAllRoles() {
        final TypedQuery<Role> query = getEntityManager().createNamedQuery("role.getAllRoles", Role.class);
        return query.getResultList();
    }

    public List<Role> getAllGroups() {
        final TypedQuery<Role> query = getEntityManager().createNamedQuery("role.getAllGroups", Role.class);
        return query.getResultList();
    }

    public Role getById(final Long id) {
        try {
            final TypedQuery<Role> query = getEntityManager().createNamedQuery("role.getById", Role.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public Role getByName(final String name) {
        try {
            final TypedQuery<Role> query = getEntityManager().createNamedQuery("role.getByName", Role.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void save(Role role) {
        if (role.getId() == null) {
            persist(role);
        } else {
            merge(role);
        }
    }
}
