package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceMember;
import eu.ill.visa.core.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class InstanceMemberRepository extends AbstractRepository<InstanceMember> {

    @Inject
    InstanceMemberRepository(final Provider<EntityManager> entityManager) {
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

    public void save(InstanceMember instanceMember) {
        if (instanceMember.getId() == null) {
            persist(instanceMember);
        } else {
            merge(instanceMember);
        }
    }
}
