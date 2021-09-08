package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.SecurityGroupFilter;
import eu.ill.visa.persistence.repositories.SecurityGroupFilterRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

@Transactional
@Singleton
public class SecurityGroupFilterService {

    @Inject
    private SecurityGroupFilterRepository repository;

    public SecurityGroupFilter getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(SecurityGroupFilter securityGroupFilter) {
        this.repository.delete(securityGroupFilter);
    }

    public void save(@NotNull SecurityGroupFilter securityGroupFilter) {
        this.repository.save(securityGroupFilter);
    }

    public List<SecurityGroupFilter> getAll() {
        return this.repository.getAll();
    }

    public List<SecurityGroupFilter> getAll(Pagination pagination) {
        return this.repository.getAll(pagination);
    }

    public Long countAll() {
        return repository.countAll();
    }
}
