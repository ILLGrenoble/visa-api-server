package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class InstanceMemberRepository extends AbstractRepository<InstanceMember> {

    @Inject
    InstanceMemberRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceMember> getAll() {
        final TypedQuery<InstanceMember> query = getEntityManager().createNamedQuery("instanceMember.getAll", InstanceMember.class);
        return query.getResultList();
    }

    public InstanceMember getById(final Long id) {
        try {
            final TypedQuery<InstanceMember> query = getEntityManager().createNamedQuery("instanceMember.getById", InstanceMember.class);
            query.setParameter("id", id);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public InstanceMember getByInstanceAndUser(Instance instance, User user) {
        try {
            final TypedQuery<InstanceMember> query = getEntityManager().createNamedQuery("instanceMember.getByInstanceAndUser", InstanceMember.class);
            query.setParameter("instance", instance);
            query.setParameter("user", user);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<InstanceMember> getAllByInstanceId(Long instanceId) {
        final TypedQuery<InstanceMember> query = getEntityManager().createNamedQuery("instanceMember.getAllByInstanceId", InstanceMember.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public List<InstanceMember> getAllByInstanceIdAndRole(Long instanceId, InstanceMemberRole role) {
        final TypedQuery<InstanceMember> query = getEntityManager().createNamedQuery("instanceMember.getAllByInstanceIdAndRole", InstanceMember.class);
        query.setParameter("instanceId", instanceId);
        query.setParameter("role", role);
        return query.getResultList();
    }

    public void save(InstanceMember instanceMember) {
        if (instanceMember.getId() == null) {
            persist(instanceMember);
        } else {
            merge(instanceMember);
        }
    }
}
