package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.SecurityGroupFilter;
import eu.ill.visa.persistence.repositories.SecurityGroupFilterRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class SecurityGroupFilterService {

    private final SecurityGroupFilterRepository repository;

    @Inject
    public SecurityGroupFilterService(final SecurityGroupFilterRepository repository) {
        this.repository = repository;
    }

    public SecurityGroupFilter getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(SecurityGroupFilter securityGroupFilter) {
        this.repository.delete(securityGroupFilter);
    }

    public void save(@NotNull SecurityGroupFilter securityGroupFilter) {
        this.repository.save(securityGroupFilter);
    }

    public SecurityGroupFilter getSecurityGroupFilterBySecurityIdAndObjectIdAndType(@NotNull final Long securityGroupId, @NotNull final Long objectId, @NotNull final String objectType) {
        return this.repository.securityGroupFilterBySecurityIdAndObjectIdAndType(securityGroupId, objectId, objectType);
    }

    public List<SecurityGroupFilter> getAll() {
        return this.repository.getAll();
    }
}
