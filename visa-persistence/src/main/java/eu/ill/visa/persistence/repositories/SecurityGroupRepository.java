package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.providers.SecurityGroupFilterProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class SecurityGroupRepository extends AbstractRepository<SecurityGroup> {

    @Inject
    SecurityGroupRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<SecurityGroup> getAll() {
        final TypedQuery<SecurityGroup> query = getEntityManager().createNamedQuery("securityGroup.getAll", SecurityGroup.class);
        return query.getResultList();
    }

    public List<SecurityGroup> getAll(QueryFilter filter,  OrderBy orderBy) {
        final SecurityGroupFilterProvider provider = new SecurityGroupFilterProvider(getEntityManager());
        List<SecurityGroup> allSecurityGroups = super.getAll(provider, filter, orderBy);

        return allSecurityGroups.stream().filter(securityGroup -> {
            CloudProviderConfiguration cloudProviderConfiguration = securityGroup.getCloudProviderConfiguration();
            if (cloudProviderConfiguration == null) {
                return true;
            }

            return cloudProviderConfiguration.getDeletedAt() == null;
        }).collect(Collectors.toList());
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

    public List<SecurityGroup> getFlavourBasedSecurityGroups(Flavour flavour) {
        if (flavour == null) {
            return new ArrayList<>();
        }

        final TypedQuery<SecurityGroup> query = getEntityManager().createNamedQuery("securityGroup.getFlavourBasedSecurityGroups", SecurityGroup.class);
        query.setParameter("flavourId", flavour.getId());

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
