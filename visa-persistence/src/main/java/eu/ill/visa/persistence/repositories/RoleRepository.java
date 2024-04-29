package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Role;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class RoleRepository extends AbstractRepository<Role> {

    private static final String FIXTURES_FILE = "fixtures/roles.sql";

    @Inject
    RoleRepository(final EntityManager entityManager) {
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
