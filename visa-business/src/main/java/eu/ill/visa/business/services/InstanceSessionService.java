package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.persistence.repositories.InstanceSessionMemberRepository;
import eu.ill.visa.persistence.repositories.InstanceSessionRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Transactional
@Singleton
public class InstanceSessionService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSessionService.class);

    private final InstanceSessionRepository repository;
    private final InstanceSessionMemberRepository instanceSessionMemberRepository;
    private final InstanceJupyterSessionService instanceJupyterSessionService;
    private final InstanceService instanceService;
    private final UserService userService;

    @Inject
    public InstanceSessionService(final InstanceSessionRepository repository,
                                  final InstanceSessionMemberRepository instanceSessionMemberRepository,
                                  final InstanceJupyterSessionService instanceJupyterSessionService,
                                  final InstanceService instanceService,
                                  final UserService userService) {
        this.repository = repository;
        this.instanceSessionMemberRepository = instanceSessionMemberRepository;
        this.instanceJupyterSessionService = instanceJupyterSessionService;
        this.instanceService = instanceService;
        this.userService = userService;
    }

    public List<InstanceSession> getAll() {
        return this.repository.getAll();
    }

    public InstanceSession getById(@NotNull Long id) {
        return this.repository.getById(id);
    }

    public InstanceSession create(@NotNull Instance instance, String protocol, String connectionId) {
        InstanceSession session = new InstanceSession(instance, protocol, connectionId);

        this.save(session);

        return session;
    }

    public InstanceSession getByInstance(@NotNull Instance instance) {
        return this.repository.getByInstance(instance);
    }

    public List<InstanceSession> getAllByInstance(@NotNull Instance instance) {
        return this.repository.getAllByInstance(instance);
    }

    public InstanceSession getByInstanceAndProtocol(@NotNull Instance instance, String protocol) {
        return this.getByInstanceIdAndProtocol(instance.getId(), protocol);
    }

    public InstanceSession getByInstanceIdAndProtocol(Long instanceId, String protocol) {
        return this.repository.getByInstanceIdAndProtocol(instanceId, protocol);
    }

    public void save(@NotNull InstanceSession instanceSession) {
        this.repository.save(instanceSession);
    }

    public void addInstanceSessionMember(@NotNull InstanceSession instanceSession, String memberId, User user, String role) {
        InstanceSessionMember sessionMember = new InstanceSessionMember(instanceSession, memberId, user, role);
        sessionMember.setActive(true);

        this.instanceSessionMemberRepository.save(sessionMember);
    }

    public void removeInstanceSessionMember(@NotNull InstanceSession instanceSession, String memberId) {
        InstanceSessionMember sessionMember = this.instanceSessionMemberRepository.getSessionMember(instanceSession, memberId);
        if (sessionMember != null) {
            sessionMember.setActive(false);
            this.instanceSessionMemberRepository.save(sessionMember);

            List<InstanceSessionMember> members = this.getAllSessionMembersByInstanceSessionId(instanceSession.getId());

            if (members.isEmpty()) {
                logger.info("Session for Instance with Id: {} is no longer current as it has no connected members", instanceSession.getInstance().getId());
                instanceSession.setCurrent(false);
            }
            this.repository.save(instanceSession);
        } else {
            logger.warn("Got a null session member (memberId {}) for instance {}", memberId, instanceSession.getInstance().getId());
        }
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstanceSession(@NotNull InstanceSession instanceSession) {
        return this.getAllSessionMembersByInstanceSessionId(instanceSession.getId());
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstanceSessionId(@NotNull Long sessionId) {
        return this.instanceSessionMemberRepository.getAllSessionMembersByInstanceSessionId(sessionId);
    }

    public List<InstanceSessionMember> getAllHistorySessionMembersByInstanceId(final Long instanceId) {
        return this.instanceSessionMemberRepository.getAllHistorySessionMembersByInstanceId(instanceId);
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstance(@NotNull Instance instance) {
        return this.getAllSessionMembersByInstanceId(instance.getId());
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstanceId(@NotNull Long instanceId) {
        return this.instanceSessionMemberRepository.getAllSessionMembersByInstanceId(instanceId);
    }

    public InstanceSessionMember getSessionMemberBySessionId(String sessionId) {
        return this.instanceSessionMemberRepository.getBySessionId(sessionId);
    }

    public void saveInstanceSessionMember(InstanceSessionMember instanceSessionMember) {
        this.instanceSessionMemberRepository.save(instanceSessionMember);
    }

    public void cleanupSession() {
        List<InstanceSession> activeSessions = this.repository.getAll();
        for (InstanceSession instanceSession : activeSessions) {
            instanceSession.setCurrent(false);
            this.repository.save(instanceSession);
        }

        List<InstanceSessionMember> activeSessionMembers = this.instanceSessionMemberRepository.getAll();
        for (InstanceSessionMember instanceSessionMember : activeSessionMembers) {
            instanceSessionMember.setActive(false);
            this.instanceSessionMemberRepository.save(instanceSessionMember);
        }

        // Do the cleanup here... not ideal but at least it is centralised
        this.instanceJupyterSessionService.cleanupSession();
    }

    public boolean canConnectWhileOwnerAway(Instance instance, String userId) {
        final User user = this.userService.getById(userId);
        boolean userIsOwner = instance.isOwner(user);

        // The user is the owner
        if (userIsOwner) {
            return true;
        }

        // If owner has selected unrestricted access to the instance then let members and support access it
        boolean userIsMember = instance.isMember(user);
        if (userIsMember && instance.canAccessWhenOwnerAway()) {
            return true;
        }

        // Check for support/admin access: can access if owner is standard user (not staff) or has granted unrestricted access
        boolean userIsSupport = this.instanceService.isInstanceSupport(user, instance);
        InstanceMember owner = instance.getOwner();
        boolean ownerIsExternalUser = !owner.getUser().hasRole(Role.STAFF_ROLE);
        if (userIsSupport) {
            return instance.canAccessWhenOwnerAway() || ownerIsExternalUser;
        }

        return false;
    }

    public InstanceMemberRole getUserSessionRole(final Instance instance, final User user) {
        requireNonNull(instance, "instance cannot be null");
        requireNonNull(user, "user cannot be null");

        final InstanceMember member = instance.getMember(user);
        if (member == null) {
            if (user.hasAnyRole(List.of(Role.ADMIN_ROLE, Role.IT_SUPPORT_ROLE, Role.INSTRUMENT_CONTROL_ROLE, Role.INSTRUMENT_SCIENTIST_ROLE))) {
                return InstanceMemberRole.SUPPORT;
            }

            return InstanceMemberRole.NONE;
        }
        return member.getRole();
    }
}
