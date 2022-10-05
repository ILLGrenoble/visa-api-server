package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.CloudProviderConfiguration;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.domain.SecurityGroupFilter;
import eu.ill.visa.persistence.providers.SecurityGroupFilterFilterProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class SecurityGroupFilterRepository extends AbstractRepository<SecurityGroupFilter> {

    @Inject
    SecurityGroupFilterRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<SecurityGroupFilter> getAll() {
        final TypedQuery<SecurityGroupFilter> query = getEntityManager().createNamedQuery("securityGroupFilter.getAll", SecurityGroupFilter.class);
        return query.getResultList();
    }

    public List<SecurityGroupFilter> getAll(QueryFilter filter, OrderBy orderBy) {
        final SecurityGroupFilterFilterProvider provider = new SecurityGroupFilterFilterProvider(getEntityManager());
        List<SecurityGroupFilter> allSecurityGroupFilters = super.getAll(provider, filter, orderBy);

        return allSecurityGroupFilters.stream().filter(securityGroupFilter -> {
            CloudProviderConfiguration cloudProviderConfiguration = securityGroupFilter.getSecurityGroup().getCloudProviderConfiguration();
            if (cloudProviderConfiguration == null) {
                return true;
            }

            return cloudProviderConfiguration.getDeletedAt() == null;
        }).collect(Collectors.toList());
    }

    public SecurityGroupFilter getById(Long id) {
        try {
            TypedQuery<SecurityGroupFilter> query = getEntityManager().createNamedQuery("securityGroupFilter.getById", SecurityGroupFilter.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(final SecurityGroupFilter securityGroupFilter) {
        remove(securityGroupFilter);
    }

    public void save(final SecurityGroupFilter securityGroupFilter) {
        if (securityGroupFilter.getId() == null) {
            persist(securityGroupFilter);
        } else {
            merge(securityGroupFilter);
        }
    }

    public SecurityGroupFilter securityGroupFilterBySecurityIdAndObjectIdAndType(Long securityGroupId, Long objectId, String objectType) {
        try {
            TypedQuery<SecurityGroupFilter> query = getEntityManager().createNamedQuery("securityGroupFilter.securityGroupFilterBySecurityIdAndObjectIdAndType", SecurityGroupFilter.class);
            query.setParameter("securityGroupId", securityGroupId);
            query.setParameter("objectId", objectId);
            query.setParameter("objectType", objectType);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }
}
