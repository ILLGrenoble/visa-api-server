package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.NoSuchElementException;

@Singleton
public class InstanceCommandRepository extends AbstractRepository<InstanceCommand> {

    @Inject
    InstanceCommandRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceCommand> getAll() {
        TypedQuery<InstanceCommand> query = getEntityManager().createNamedQuery("instanceCommand.getAll", InstanceCommand.class);
        return query.getResultList();
    }

    public List<InstanceCommand> getAllActive() {
        TypedQuery<InstanceCommand> query = getEntityManager().createNamedQuery("instanceCommand.getAllActive", InstanceCommand.class);
        return query.getResultList();
    }

    public List<InstanceCommand> getAllPending() {
        TypedQuery<InstanceCommand> query = getEntityManager().createNamedQuery("instanceCommand.getAllPending", InstanceCommand.class);
        return query.getResultList();
    }

    public InstanceCommand getById(Long id) {
        TypedQuery<InstanceCommand> query = getEntityManager().createNamedQuery("instanceCommand.getById", InstanceCommand.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public void delete(InstanceCommand instanceCommand) {
        remove(instanceCommand);
    }

    public List<InstanceCommand> getAllForUser(User user) {
        TypedQuery<InstanceCommand> query = getEntityManager().createNamedQuery("instanceCommand.getAllForUser", InstanceCommand.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<InstanceCommand> getAllForInstance(Instance instance) {
        TypedQuery<InstanceCommand> query = getEntityManager().createNamedQuery("instanceCommand.getAllForInstance", InstanceCommand.class);
        query.setParameter("instance", instance);
        return query.getResultList();
    }

    public InstanceCommand getLastUserCommandForInstance(Instance instance) {
        TypedQuery<InstanceCommand> query = getEntityManager().createNamedQuery("instanceCommand.getAllUserCommandsForInstance", InstanceCommand.class);
        query.setParameter("instance", instance);
        try {
            return query.getResultList().getLast();
        } catch (NoSuchElementException exception) {
            return null;
        }
    }

    public void save(InstanceCommand instanceCommand) {
        if (instanceCommand.getId() == null) {
            persist(instanceCommand);

        } else {
            merge(instanceCommand);
        }
    }
}
