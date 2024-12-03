package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial;
import eu.ill.visa.persistence.repositories.InstanceSessionMemberRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
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

    public void create(@NotNull InstanceSession instanceSession, String memberId, User user, InstanceMemberRole role) {
        // Ensure we do not have any previous InstanceSessionMembers that have not been deactivated correctly
        InstanceSessionMemberPartial instanceSessionMemberPartial = this.getPartialByInstanceSessionIdAndSessionId(instanceSession.getId(), memberId);
        if (instanceSessionMemberPartial != null) {
            instanceSessionMemberPartial.setActive(false);
            this.updatePartial(instanceSessionMemberPartial);
            logger.warn("Deleting a InstanceSessionMember for instance {} with session Id {} that was not previously deleted", instanceSession.getInstanceId(), memberId);
        }

        InstanceSessionMember sessionMember = new InstanceSessionMember(instanceSession, memberId, user, role);
        sessionMember.setActive(true);

        this.repository.save(sessionMember);
    }

    public List<InstanceSessionMember> getAllByInstanceSessionId(@NotNull Long sessionId) {
        return this.repository.getAllByInstanceSessionId(sessionId);
    }

    public List<InstanceSessionMember> getAllHistorySessionMembersByInstanceId(final Long instanceId) {
        return this.repository.getAllHistorySessionMembersByInstanceId(instanceId);
    }

    public List<InstanceSessionMember> getAllByInstance(@NotNull Instance instance) {
        return this.getAllByInstanceId(instance.getId());
    }

    public List<InstanceSessionMember> getAllByInstanceId(@NotNull Long instanceId) {
        return this.repository.getAllByInstanceId(instanceId);
    }

    public List<List<InstanceSessionMember>> getAllByInstanceIds(@NotNull List<Long> instanceIds) {
        List<InstanceSessionMember> ungroupedSessionMembers = this.repository.getAllByInstanceIds(instanceIds);
        return instanceIds.stream().map(id -> {
            return ungroupedSessionMembers.stream().filter(sessionMember -> sessionMember.getInstanceSession().getInstanceId().equals(id)).toList();
        }).toList();
    }

    public List<InstanceSessionMemberPartial> getAllPartials() {
        return this.repository.getAllPartials();
    }

    public List<InstanceSessionMemberPartial> getAllPartialsByInstanceSessionId(@NotNull Long sessionId) {
        return this.repository.getAllPartialsByInstanceSessionId(sessionId);
    }

    public List<InstanceSessionMemberPartial> getAllPartialsByInstanceId(@NotNull Long instanceId) {
        return this.repository.getAllPartialsByInstanceId(instanceId);
    }

    public InstanceSessionMemberPartial getPartialByInstanceSessionIdAndSessionId(Long instanceSessionId, final String sessionId) {
        return this.repository.getPartialByInstanceSessionIdAndSessionId(instanceSessionId, sessionId);

    }

    public void updatePartial(final InstanceSessionMemberPartial instanceSessionMember) {
        this.repository.updatePartial(instanceSessionMember);
    }

}
