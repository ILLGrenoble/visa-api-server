package eu.ill.visa.business.services;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
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
@ApplicationScoped
public class InstanceSessionMemberService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSessionMemberService.class);

    private final InstanceSessionMemberRepository repository;

    @Inject
    public InstanceSessionMemberService(final InstanceSessionMemberRepository repository) {
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
