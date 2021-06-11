package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class InstanceCommandRepository extends AbstractRepository<InstanceCommand> {

    @Inject
    InstanceCommandRepository(final Provider<EntityManager> entityManager) {
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

    public void save(InstanceCommand instanceCommand) {
        if (instanceCommand.getId() == null) {
            persist(instanceCommand);

        } else {
            merge(instanceCommand);
        }
    }
}
