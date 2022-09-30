package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.domain.SecurityGroup;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.persistence.providers.SecurityGroupFilterProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SecurityGroupRepository extends AbstractRepository<SecurityGroup> {

    @Inject
    SecurityGroupRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<SecurityGroup> getAll() {
        final TypedQuery<SecurityGroup> query = getEntityManager().createNamedQuery("securityGroup.getAll", SecurityGroup.class);
        return query.getResultList();
    }

    public List<SecurityGroup> getAll(QueryFilter filter,  OrderBy orderBy) {
        final SecurityGroupFilterProvider provider = new SecurityGroupFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy);
    }

    public SecurityGroup getById(Long id) {
        try {
            TypedQuery<SecurityGroup> query = getEntityManager().createNamedQuery("securityGroup.getById", SecurityGroup.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (
            NoResultException exception) {
            return null;
        }
    }

    public List<SecurityGroup> getDefaultSecurityGroups() {
        final TypedQuery<SecurityGroup> query = getEntityManager().createNamedQuery("securityGroup.getDefaultSecurityGroups", SecurityGroup.class);

        return query.getResultList();
    }

    public List<SecurityGroup> getRoleBasedSecurityGroups(User user) {
        if (user == null) {
            return new ArrayList<>();
        }

        final TypedQuery<SecurityGroup> query = getEntityManager().createNamedQuery("securityGroup.getRoleBasedSecurityGroups", SecurityGroup.class);
        query.setParameter("userId", user.getId());

        return query.getResultList();
    }

    public void delete(final SecurityGroup securityGroup) {
        remove(securityGroup);
    }

    public void save(final SecurityGroup securityGroup) {
        if (securityGroup.getId() == null) {
            persist(securityGroup);
        } else {
            merge(securityGroup);
        }
    }
}
