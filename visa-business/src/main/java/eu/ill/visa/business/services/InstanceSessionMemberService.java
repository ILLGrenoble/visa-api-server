package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.persistence.repositories.InstanceSessionMemberRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Transactional
@Singleton
public class InstanceSessionMemberService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSessionMemberService.class);

    private final InstanceSessionMemberRepository repository;

    @Inject
    public InstanceSessionMemberService(final InstanceSessionMemberRepository repository) {
        this.repository = repository;
    }

    public List<InstanceSessionMember> getAll(Pagination pagination) {
        return this.repository.getAll(pagination);
    }

    public Long countAll() {
        return repository.countAll();
    }

    public Long countAllActive() {
        return repository.countAllActive();
    }
}
