package eu.ill.visa.business.services;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.InstanceSessionMember;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.persistence.repositories.InstanceSessionMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class InstanceSessionMemberService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSessionMemberService.class);

    private InstanceSessionMemberRepository repository;

    @Inject
    public InstanceSessionMemberService(InstanceSessionMemberRepository repository) {
        this.repository = repository;
    }

    public List<InstanceSessionMember> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }

    public Long countAllActive(QueryFilter filter) {
        return repository.countAllActive(requireNonNullElseGet(filter, QueryFilter::new));
    }
}
