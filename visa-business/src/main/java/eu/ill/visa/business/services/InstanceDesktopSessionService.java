package eu.ill.visa.business.services;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.persistence.models.Tuple2;
import eu.ill.visa.persistence.repositories.InstanceDesktopSessionRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Singleton
public class InstanceDesktopSessionService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceDesktopSessionService.class);

    private final InstanceDesktopSessionRepository repository;
    private final InstanceService instanceService;
    private final BookingRequestService bookingRequestService;
    private final InstanceJupyterSessionService instanceJupyterSessionService;

    @Inject
    public InstanceDesktopSessionService(final InstanceDesktopSessionRepository repository,
                                         final InstanceService instanceService,
                                         final BookingRequestService bookingRequestService,
                                         final InstanceJupyterSessionService instanceJupyterSessionService) {
        this.repository = repository;
        this.instanceService = instanceService;
        this.bookingRequestService = bookingRequestService;
        this.instanceJupyterSessionService = instanceJupyterSessionService;
    }

    public InstanceDesktopSession getById(final Long id) {
        return this.repository.getById(id);
    }

    public List<InstanceDesktopSession> getAll() {
        return this.getAll(null);
    }

    public List<InstanceDesktopSession> getAll(Pagination pagination) {
        return this.repository.getAll(pagination);
    }

    public Long countAll() {
        return this.repository.countAll();
    }

    public Long countAllActive() {
        return this.repository.countAllActive();
    }

    public List<InstanceDesktopSession> getAllByInstanceId(final Long instanceId) {
        return this.repository.getAllByInstanceId(instanceId);
    }

    public InstanceDesktopSession getLatestByInstanceId(Long instanceId) {
        return this.repository.getLatestByInstanceId(instanceId);
    }

    public List<InstanceDesktopSession> getAllByUserId(final String userId) {
        return this.repository.getAllByUserId(userId);
    }

    public List<List<InstanceDesktopSession>> getAllByInstanceIds(final List<Long> instanceIds) {
        List<InstanceDesktopSession> ungroupedDesktopSessions = this.repository.getAllByInstanceIds(instanceIds);
        return instanceIds.stream().map(id -> {
            return ungroupedDesktopSessions.stream().filter(desktopSession -> desktopSession.getInstanceId().equals(id)).toList();
        }).toList();
    }

    public List<InstanceDesktopSession> getAllHistoricalByInstanceId(final Long instanceId) {
        return this.repository.getAllHistoricalByInstanceId(instanceId);
    }

    public List<InstanceDesktopSession> getAllByConnectionId(final String connectionId) {
        return this.repository.getAllByConnectionId(connectionId);
    }

    public List<Tuple2<InstanceDesktopSession, ConnectedUser>> getAllWithConnectedUserByConnectionId(final String connectionId) {
        try {
            return this.repository.getAllWithConnectedUserByConnectionId(connectionId);

        } catch (Exception e) {
            logger.error("Failed to getAllByConnectionId: {} ", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ConnectedUser> getAllConnectedUsersByConnectionId(final String connectionId) {
        return this.repository.getAllConnectedUsersByConnectionId(connectionId);
    }

    public List<ConnectedUser> getAllConnectedUsersByInstanceId(final Long instanceId) {
        return this.repository.getAllConnectedUsersByInstanceId(instanceId);
    }

    public InstanceDesktopSession getByConnectionIdAndClientId(final String connectionId, final String clientId) {
        return this.repository.getByConnectionIdAndClientId(connectionId, clientId);
    }

    public void close(final InstanceDesktopSession instanceDesktopSession) {
        this.repository.close(instanceDesktopSession);
    }

    public void updateClientPingAt(final InstanceDesktopSession instanceDesktopSession) {
        this.repository.updateClientPingAt(instanceDesktopSession);
    }

    public void updateInteractionAt(final InstanceDesktopSession instanceDesktopSession) {
        this.repository.updateInteractionAt(instanceDesktopSession);
    }

    public boolean isOwnerConnected(final String connectionId) {
        if (connectionId == null) {
            return false;
        }

        List<InstanceDesktopSession> sessions = this.getAllByConnectionId(connectionId);
        return sessions.stream()
            .anyMatch(instanceDesktopSession -> instanceDesktopSession.getRole().equals(InstanceMemberRole.OWNER));

    }

    public InstanceDesktopSession create(final String connectionId, final String clientId, final Long instanceId, final String userId, final InstanceMemberRole role, final String protocol) {
        // Ensure we do not have any previous InstanceDesktopSessions that have not been closed correctly
        int previouslyActiveSessions = this.repository.closeAllByInstanceIdAndConnectionIdAndClientId(instanceId, connectionId, clientId);
        if (previouslyActiveSessions > 0) {
            logger.warn("Deleted {} InstanceDesktopSession for instance {} with client Id {} that were previously not deleted", previouslyActiveSessions, instanceId, clientId);
        }

        InstanceDesktopSession instanceDesktopSession = new InstanceDesktopSession(instanceId, userId, connectionId, clientId, role, protocol);
        this.repository.save(instanceDesktopSession);

        return instanceDesktopSession;
    }

    public InstanceMemberRole getUserSessionRole(final Instance instance, final User user) {
        final InstanceMember member = instance.getMember(user);
        if (member == null) {
            if (user.hasAnyRoleWithName(List.of(Role.ADMIN_ROLE, Role.IT_SUPPORT_ROLE, Role.INSTRUMENT_CONTROL_ROLE, Role.INSTRUMENT_SCIENTIST_ROLE))) {
                return InstanceMemberRole.SUPPORT;

            } else if (instance.getBookingTokenId() != null) {
                BookingRequest bookingRequest = this.bookingRequestService.getByBookingTokenId(instance.getBookingTokenId());
                if (bookingRequest.isOwnerOrOrganiser(user)) {
                    return InstanceMemberRole.SUPPORT;
                }
            }

            return InstanceMemberRole.NONE;
        }
        return member.getRole();
    }


    public boolean canConnectWhileOwnerAway(final Instance instance, final User user) {
        if (instance.isOwner(user)) {
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
        boolean ownerIsExternalUser = !owner.getUser().hasRoleWithName(Role.STAFF_ROLE);
        if (userIsSupport) {
            return instance.canAccessWhenOwnerAway() || (instance.getBookingTokenId() == null && ownerIsExternalUser);
        }

        return false;
    }

    public void closeAllSessions() {
        this.repository.closeAll();

        // Do the cleanup here... not ideal but at least it is centralised
        this.instanceJupyterSessionService.cleanupSession();
    }
}
