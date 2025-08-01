package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial;
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
    private final InstanceSessionMemberService instanceSessionMemberService;
    private final InstanceJupyterSessionService instanceJupyterSessionService;
    private final InstanceService instanceService;
    private final UserService userService;

    @Inject
    public InstanceSessionService(final InstanceSessionRepository repository,
                                  final InstanceSessionMemberService instanceSessionMemberService,
                                  final InstanceJupyterSessionService instanceJupyterSessionService,
                                  final InstanceService instanceService,
                                  final UserService userService) {
        this.repository = repository;
        this.instanceSessionMemberService = instanceSessionMemberService;
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

    public InstanceSession create(@NotNull Long instanceId, String protocol, String connectionId) {
        InstanceSession session = new InstanceSession(instanceId, protocol, connectionId);

        this.save(session);

        return session;
    }

    public InstanceSession getByInstance(@NotNull Instance instance) {
        return this.repository.getByInstance(instance);
    }

    public InstanceSession getLastByInstance(@NotNull Instance instance) {
        return this.repository.getLastByInstance(instance);
    }

    public List<InstanceSession> getAllByInstance(@NotNull Instance instance) {
        return this.repository.getAllByInstance(instance);
    }

    public InstanceSession getLatestByInstanceAndProtocol(@NotNull Instance instance, String protocol) {
        return this.getLatestByInstanceIdAndProtocol(instance.getId(), protocol);
    }

    public InstanceSession getLatestByInstanceIdAndProtocol(Long instanceId, String protocol) {
        // Fixes bug when 2 requests are made at the same time to create a session (on load-balanced servers)
        List<InstanceSession>  instanceSessions = this.repository.getAllLatestByInstanceIdAndProtocol(instanceId, protocol);
        if (instanceSessions.isEmpty()) {
            return null;
        }

        // Get the latest session
        InstanceSession latestInstanceSession = instanceSessions.removeFirst();

        // Remove any remaining sessions
        for (InstanceSession instanceSession : instanceSessions) {
            logger.warn("Multiple instance sessions were available for the instance {} and protocol {}: deleting instance session {}", instanceId, protocol, instanceSession.getId());
            instanceSession.setCurrent(false);
            this.save(instanceSession);
        }

        return latestInstanceSession;
    }

    public void deleteSessionMember(@NotNull InstanceSession instanceSession, String clientId) {
        InstanceSessionMemberPartial sessionMember = this.instanceSessionMemberService.getPartialByInstanceSessionIdAndClientId(instanceSession.getId(), clientId);
        if (sessionMember != null) {
            this.instanceSessionMemberService.deactivateSessionMember(sessionMember);

            List<InstanceSessionMemberPartial> members = this.instanceSessionMemberService.getAllPartialsByInstanceSessionId(instanceSession.getId());
            if (members.isEmpty() && instanceSession.getProtocol().equals(InstanceSession.GUACAMOLE_PROTOCOL)) {
                logger.info("Session for Instance with Id: {} is no longer current as it has no connected members", instanceSession.getInstanceId());
                instanceSession.setCurrent(false);
                this.updatePartial(instanceSession);
            }

        } else {
            logger.warn("Got a null session member (clientId {}) for instance {}", clientId, instanceSession.getInstanceId());
        }
    }

    public void save(@NotNull InstanceSession instanceSession) {
        this.repository.save(instanceSession);
    }

    public void updatePartial(@NotNull InstanceSession instanceSession) {
        this.repository.updatePartial(instanceSession);
    }

    public void cleanupForInstance(@NotNull Instance instance) {
        requireNonNull(instance, "instance cannot be null");

        this.instanceSessionMemberService.getAllByInstance(instance).forEach(member -> {;
            // Remove all active session members
            this.deleteSessionMember(member.getInstanceSession(), member.getClientId());
        });
        this.getAllByInstance(instance).forEach(session -> {
            session.setCurrent(false);
            this.updatePartial(session);
        });
    }

    public void cleanupSession() {
        List<InstanceSession> activeSessions = this.repository.getAll();
        for (InstanceSession instanceSession : activeSessions) {
            instanceSession.setCurrent(false);
            this.repository.save(instanceSession);
        }

        List<InstanceSessionMemberPartial> activeSessionMembers = this.instanceSessionMemberService.getAllPartials();
        for (InstanceSessionMemberPartial instanceSessionMember : activeSessionMembers) {
            this.instanceSessionMemberService.deactivateSessionMember(instanceSessionMember);
        }

        // Do the cleanup here... not ideal but at least it is centralised
        this.instanceJupyterSessionService.cleanupSession();
    }

    public boolean canConnectWhileOwnerAway(Instance instance, String userId) {
        final User user = this.userService.getById(userId);
        return this.canConnectWhileOwnerAway(instance, user);
    }

    public boolean canConnectWhileOwnerAway(Instance instance, User user) {
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
