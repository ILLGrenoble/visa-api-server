package eu.ill.visa.business.services;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceMember;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.persistence.repositories.InstanceMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Transactional
@Singleton
public class InstanceMemberService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceMemberService.class);

    private InstanceMemberRepository repository;

    @Inject
    public InstanceMemberService(InstanceMemberRepository repository) {
        this.repository = repository;
    }

    public List<InstanceMember> getAll() {
        return this.repository.getAll();
    }

    public InstanceMember getById(@NotNull Long id) {
        return this.repository.getById(id);
    }

    public InstanceMember getByInstanceAndUser(Instance instance, User user) {
        return this.repository.getByInstanceAndUser(instance, user);
    }

    public void save(@NotNull InstanceMember instanceMember) {
        this.repository.save(instanceMember);
    }
}
