package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.*;
import eu.ill.visa.persistence.repositories.InstanceSessionMemberRepository;
import eu.ill.visa.persistence.repositories.InstanceSessionRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

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

    public InstanceSession create(@NotNull Instance instance, String connectionId) {
        InstanceSession session = new InstanceSession(instance, connectionId);

        this.save(session);

        return session;
    }

    public InstanceSession getByInstance(@NotNull Instance instance) {
        return this.repository.getByInstance(instance);
    }

    public List<InstanceSession> getAllByInstance(@NotNull Instance instance) {
        return this.repository.getAllByInstance(instance);
    }

    public void save(@NotNull InstanceSession instanceSession) {
        this.repository.save(instanceSession);
    }

    public void addInstanceSessionMember(@NotNull InstanceSession instanceSession, UUID sessionId, User user, String role) {
        InstanceSessionMember sessionMember = new InstanceSessionMember(instanceSession, sessionId.toString(), user, role);
        sessionMember.setActive(true);

        this.instanceSessionMemberRepository.save(sessionMember);
    }

    public void removeInstanceSessionMember(@NotNull InstanceSession instanceSession, UUID sessionId) {
        InstanceSessionMember sessionMember = this.instanceSessionMemberRepository.getSessionMember(instanceSession, sessionId.toString());
        if (sessionMember != null) {
            sessionMember.setActive(false);
            this.instanceSessionMemberRepository.save(sessionMember);

            List<InstanceSessionMember> members = this.instanceSessionMemberRepository.getAllSessionMembers(instanceSession);

            if (members.size() == 0) {
                logger.info("Session for Instance with Id: {} is no longer current as it has no connected members", instanceSession.getInstance().getId());
                instanceSession.setCurrent(false);
            }
            this.repository.save(instanceSession);
        } else {
            logger.warn("Got a null session member (session {}) for instance {}", sessionId.toString(), instanceSession.getInstance().getId());
        }
    }

    public List<InstanceSessionMember> getAllSessionMembers(@NotNull InstanceSession instanceSession) {
        return this.instanceSessionMemberRepository.getAllSessionMembers(instanceSession);
    }

    public List<InstanceSessionMember> getAllHistorySessionMembersByInstanceId(final Long instanceId) {
        return this.instanceSessionMemberRepository.getAllHistorySessionMembersByInstanceId(instanceId);
    }

    public List<InstanceSessionMember> getAllSessionMembers(@NotNull Instance instance) {
        return this.instanceSessionMemberRepository.getAllSessionMembers(instance);
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstanceId(@NotNull Long instanceId) {
        return this.instanceSessionMemberRepository.getAllSessionMembersByInstanceId(instanceId);
    }

    public InstanceSessionMember getSessionMemberBySessionId(UUID sessionId) {
        return this.instanceSessionMemberRepository.getBySessionId(sessionId.toString());
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
}
