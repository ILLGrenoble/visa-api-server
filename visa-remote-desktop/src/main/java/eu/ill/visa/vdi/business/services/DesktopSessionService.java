package eu.ill.visa.vdi.business.services;

import eu.ill.visa.business.services.InstanceExpirationService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.messages.*;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.*;
import eu.ill.visa.vdi.gateway.events.UserConnectedEvent;
import eu.ill.visa.vdi.gateway.events.UserDisconnectedEvent;
import eu.ill.visa.vdi.gateway.events.UsersConnectedEvent;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static eu.ill.visa.vdi.domain.models.SessionEvent.*;

@Startup
@ApplicationScoped
public class DesktopSessionService {

    private final static Logger logger = LoggerFactory.getLogger(DesktopSessionService.class);

    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;
    private final UserService userService;
    private final GuacamoleDesktopService guacamoleDesktopService;
    private final WebXDesktopService webXDesktopService;
    private final InstanceExpirationService instanceExpirationService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;

    private final RemoteDesktopBroker remoteDesktopBroker;

    private final List<PendingDesktopSessionMember> pendingDesktopSessionMembers = new ArrayList<>();
    private final List<DesktopSession> desktopSessions = new ArrayList<>();

    @Inject
    public DesktopSessionService(final InstanceService instanceService,
                                 final InstanceSessionService instanceSessionService,
                                 final UserService userService,
                                 final GuacamoleDesktopService guacamoleDesktopService,
                                 final WebXDesktopService webXDesktopService,
                                 final InstanceExpirationService instanceExpirationService,
                                 final VirtualDesktopConfiguration virtualDesktopConfiguration,
                                 final jakarta.enterprise.inject.Instance<RemoteDesktopBroker> remoteDesktopBrokerInstance) {
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.userService = userService;
        this.guacamoleDesktopService = guacamoleDesktopService;
        this.webXDesktopService = webXDesktopService;
        this.instanceExpirationService = instanceExpirationService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;

        this.remoteDesktopBroker = remoteDesktopBrokerInstance.get();

        this.remoteDesktopBroker.subscribe(UserConnectedMessage.class)
            .next((message) -> this.onUserConnected(message.sessionId(), message.desktopSessionMemberId(), message.user()));
        this.remoteDesktopBroker.subscribe(UserDisconnectedMessage.class)
            .next((message) -> this.onUserDisconnected(message.sessionId(), message.desktopSessionMemberId(), message.user()));
        this.remoteDesktopBroker.subscribe(AccessRevokedMessage.class)
            .next((message) -> this.onAccessRevoked(message.sessionId(), message.userId()));
        this.remoteDesktopBroker.subscribe(SessionClosedMessage.class)
            .next((message) -> this.onSessionClosed(message.sessionId()));
        this.remoteDesktopBroker.subscribe(SessionLockedMessage.class)
            .next((message) -> this.onSessionLocked(message.sessionId(), message.user()));
        this.remoteDesktopBroker.subscribe(SessionUnlockedMessage.class)
            .next((message) -> this.onSessionUnlocked(message.sessionId()));
    }

    @Shutdown
    public void shutdown() {
        this.remoteDesktopBroker.shutdown();
    }

    public synchronized DesktopSessionMember createDesktopSessionMember(final SocketClient client, final PendingDesktopSessionMember pendingDesktopSessionMember) throws OwnerNotConnectedException, UnauthorizedException, ConnectionException {
        final ConnectedUser user = pendingDesktopSessionMember.connectedUser();
        final Long instanceId = pendingDesktopSessionMember.instanceId();
        final Instance instance = this.instanceService.getFullById(instanceId);
        if (instance == null) {
            throw new ConnectionException(String.format("Instance %d no longer exists for connection", instanceId));
        }

        final EventChannel eventChannel = pendingDesktopSessionMember.eventChannel();
        final String token = pendingDesktopSessionMember.token();
        final String protocol = pendingDesktopSessionMember.protocol();

        final InstanceMemberRole role = user.getRole();
        if (role == InstanceMemberRole.NONE) {
            throw new UnauthorizedException("User " + user.getFullName() + " is unauthorised to access the instance " + instanceId);
        }

        // Create RemoteDesktopConnection (guacamole or webx)
        boolean isWebX = protocol != null && protocol.equals(DesktopService.WEBX_PROTOCOL);
        final RemoteDesktopConnection remoteDesktopConnection;
        if (isWebX) {
            logger.info("User {} creating WebX desktop connection to instance {}", (user.getFullName() + " (" + role.toString() + ")"), instance.getId());
            remoteDesktopConnection = webXDesktopService.connect(client, instance, user);
        } else {
            logger.info("User {} creating Guacamole desktop connection to instance {}", (user.getFullName() + " (" + role.toString() + ")"), instance.getId());
            remoteDesktopConnection = guacamoleDesktopService.connect(client, instance, user);
        }

        // Get or create a new DesktopSession: use the ID of the InstanceSession
        final InstanceSession instanceSession = this.instanceSessionService.getByInstanceAndProtocol(instance, protocol);
        DesktopSession desktopSession = this.getOrCreateDesktopSession(instanceSession.getId(), instance.getId(), protocol);

        // Create session member
        DesktopSessionMember desktopSessionMember = new DesktopSessionMember(token, user, eventChannel, remoteDesktopConnection, desktopSession);
        desktopSession.addMember(desktopSessionMember);

        boolean unlockSession = virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)
            && role.equals(InstanceMemberRole.OWNER)
            && !this.isOwnerConnected(instanceSession.getId());

        // Update the connected clients of the session
        this.instanceSessionService.addInstanceSessionMember(instanceSession, desktopSessionMember.getToken(), this.userService.getById(user.getId()), role.toString());

        // Remove instance from instance_expiration table if it is there due to inactivity
        this.instanceExpirationService.onInstanceActivated(instanceSession.getInstance());

        if (unlockSession) {
            // Unlock session for all clients
            this.unlockSession(desktopSession.getSessionId());

        } else {
            this.connectUser(desktopSession.getSessionId(), desktopSessionMember.getToken(), user);
        }

        return desktopSessionMember;
    }

    public synchronized void onDesktopMemberDisconnect(final DesktopSessionMember desktopSessionMember) {
        desktopSessionMember.getRemoteDesktopConnection().getConnectionThread().closeTunnel();

        DesktopSession desktopSession = desktopSessionMember.getSession();
        final Long sessionId = desktopSession.getSessionId();
        InstanceSession session = instanceSessionService.getById(sessionId);

        this.removeDesktopSessionMember(desktopSession, desktopSessionMember);

        if (session != null) {
            // Remove client/member from instance session
            instanceSessionService.removeInstanceSessionMember(session, desktopSessionMember.getToken());

            if (desktopSessionMember.isRole(InstanceMemberRole.OWNER) && !this.isOwnerConnected(sessionId)) {
                if (this.virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)) {
                    this.lockSession(sessionId, desktopSessionMember.getConnectedUser());

                } else {
                    logger.info("There is no owner ar admin connected so all clients will be disconnected");
                    this.closeSession(sessionId);
                }

            } else {
                // broadcast events for a user disconnected and current users
                this.disconnectUser(sessionId, desktopSessionMember.getToken(),desktopSessionMember.getConnectedUser());
            }

        } else {
            logger.info("There is no active instance session so all clients will be disconnected");
            this.closeSession(sessionId);
        }
    }

    public boolean isOwnerConnected(final Long sessionId) {
        List<InstanceSessionMember> instanceSessionMembers = this.instanceSessionService.getAllSessionMembersByInstanceSessionId(sessionId);
        for (final InstanceSessionMember instanceSessionMember : instanceSessionMembers) {
            String role = instanceSessionMember.getRole();
            if (role.equals(InstanceMemberRole.OWNER.toString())) {
                return true;
            }
        }

        return false;
    }

    public void connectUser(final Long sessionId, final String desktopSessionMemberId, final ConnectedUser user) {
        this.remoteDesktopBroker.broadcast(new UserConnectedMessage(sessionId, desktopSessionMemberId, user));
    }

    public void disconnectUser(final Long sessionId, final String desktopSessionMemberId, final ConnectedUser user) {
        this.remoteDesktopBroker.broadcast(new UserDisconnectedMessage(sessionId, desktopSessionMemberId, user));
    }

    public void revokeUserAccess(final DesktopSessionMember ownerSessionMember, final String userId) {
        // Verify that we have a connection and that the user is the owner
        if (ownerSessionMember.getConnectedUser().getRole().equals(InstanceMemberRole.OWNER)) {
            this.remoteDesktopBroker.broadcast(new AccessRevokedMessage(ownerSessionMember.getSession().getSessionId(), userId));
        }
    }

    public void closeSession(final Long sessionId) {
        this.remoteDesktopBroker.broadcast(new SessionClosedMessage(sessionId));
    }

    public void lockSession(final Long sessionId, final ConnectedUser user) {
        this.remoteDesktopBroker.broadcast(new SessionLockedMessage(sessionId, user));
    }

    public void unlockSession(final Long sessionId) {
        this.remoteDesktopBroker.broadcast(new SessionUnlockedMessage(sessionId));
    }

    public synchronized Optional<DesktopSession> findDesktopSession(final Long sessionId) {
        return this.desktopSessions.stream()
            .filter(desktopSession -> desktopSession.getSessionId().equals(sessionId))
            .findAny();
    }

    public synchronized void addPendingDesktopSessionMember(final PendingDesktopSessionMember pendingDesktopSessionMember) {
        this.pendingDesktopSessionMembers.add(pendingDesktopSessionMember);
    }

    public synchronized Optional<PendingDesktopSessionMember> getPendingDesktopSessionMember(final String token) {
        return this.pendingDesktopSessionMembers.stream()
            .filter(item -> item.token().equals(token))
            .findFirst();
    }

    public synchronized void removePendingDesktopSessionMember(final PendingDesktopSessionMember pendingDesktopSessionMember) {
        this.pendingDesktopSessionMembers.remove(pendingDesktopSessionMember);
    }

    public synchronized Optional<DesktopSessionMember> findDesktopSessionMember(final SocketClient client) {
        return this.desktopSessions.stream()
            .flatMap(desktopSession -> desktopSession.getMembers().stream())
            .filter(desktopSessionMember -> desktopSessionMember.getRemoteDesktopConnection().getClient().equals(client))
            .findAny();
    }

    public synchronized Optional<DesktopSessionMember> findDesktopSessionMemberByToken(final String token) {
        return this.desktopSessions.stream()
            .flatMap(desktopSession -> desktopSession.getMembers().stream())
            .filter(desktopSessionMember -> desktopSessionMember.getToken().equals(token))
            .findAny();
    }
    private void onUserConnected(final Long sessionId, final String desktopSessionMemberId, final ConnectedUser user) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstanceId();
            final String protocol = desktopSession.getProtocol();
            logger.info("User {} connected to instance {} with protocol {}", user, instanceId, protocol);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.getToken().equals(desktopSessionMemberId))
                .forEach(desktopSessionMember -> {
                    desktopSessionMember.sendEvent(USER_CONNECTED_EVENT, new UserConnectedEvent(user, instanceId));
                });

            this.sendUsersConnectedEvent(desktopSession);
        });
    }

    private void onUserDisconnected(final Long sessionId, final String desktopSessionMemberId, final ConnectedUser user) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstanceId();
            final String protocol = desktopSession.getProtocol();
            logger.info("User {} disconnected from instance {} with protocol {}", user, instanceId, protocol);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.getToken().equals(desktopSessionMemberId))
                .forEach(desktopSessionMember -> {
                    desktopSessionMember.sendEvent(USER_DISCONNECTED_EVENT, new UserDisconnectedEvent(user, instanceId));
                });

            this.sendUsersConnectedEvent(desktopSession);
        });
    }

    private void onAccessRevoked(final Long sessionId, final String userId) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstanceId();
            logger.info("Access revoked for user with id: {} in instance {}", userId, instanceId);

            desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.getConnectedUser().getId().equals(userId))
                .forEach(desktopSessionMember -> {
                    logger.info("Revoking access to remote desktop for instance {} with protocol {} for user {} with connection ID {}", instanceId, desktopSession.getProtocol(), desktopSessionMember.getConnectedUser().getFullName(), desktopSessionMember.getToken());
                    desktopSessionMember.sendEvent(ACCESS_REVOKED_EVENT);
                    desktopSessionMember.disconnect();
                });
        });
    }

    private void onSessionClosed(final Long sessionId) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            logger.info("Session closed (owner away) for instance {} with protocol {}: disconnecting all clients", desktopSession.getInstanceId(), desktopSession.getProtocol());

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.getConnectedUser().getRole().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    desktopSessionMember.sendEvent(OWNER_AWAY_EVENT);
                    desktopSessionMember.disconnect();
                });
        });
    }

    private void onSessionLocked(final Long sessionId, final ConnectedUser user) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            logger.info("Session locked (owner away) for instance {} with protocol {}: making all clients read-only", desktopSession.getInstanceId(), desktopSession.getProtocol());
            desktopSession.setLocked(true);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.getConnectedUser().getRole().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    // Check that the events channel is connected: if not connected disconnect the remote desktop
                    if (desktopSessionMember.isEventChannelOpen()) {
                        final Instance instance = this.instanceService.getFullById(desktopSession.getInstanceId());
                        if (desktopSessionMember.getConnectedUser().isRole(InstanceMemberRole.SUPPORT) && instanceSessionService.canConnectWhileOwnerAway(instance, desktopSessionMember.getConnectedUser().getId())) {
                            desktopSessionMember.sendEvent(USER_DISCONNECTED_EVENT, new UserDisconnectedEvent(user, desktopSession.getInstanceId()));

                        } else {
                            desktopSessionMember.sendEvent(SESSION_LOCKED_EVENT);
                        }

                    } else {
                        logger.info("Event channel for Desktop Session Member {} is not open so disconnecting the user", desktopSessionMember.toString());
                        desktopSessionMember.disconnect();
                    }

                });

            this.sendUsersConnectedEvent(desktopSession);
        });
    }

    private void onSessionUnlocked(final Long sessionId) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            logger.info("Session unlocked (owner back) for instance {} with protocol {}: all clients resume original roles", desktopSession.getInstanceId(), desktopSession.getProtocol());
            desktopSession.setLocked(false);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.getConnectedUser().getRole().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    desktopSessionMember.sendEvent(SESSION_UNLOCKED_EVENT);
                });

            this.sendUsersConnectedEvent(desktopSession);
        });
    }

    private void sendUsersConnectedEvent(final DesktopSession desktopSession) {
        final Long instanceId = desktopSession.getInstanceId();
        final String protocol = desktopSession.getProtocol();
        List<ConnectedUser> users = this.getConnectedUsers(desktopSession);
        if (!users.isEmpty()) {
            logger.info("Instance {} with protocol {} has the following users connected: {}", instanceId, protocol, users.stream().map(ConnectedUser::toString).toList());
            desktopSession.getMembers().forEach(desktopSessionMember -> {
                desktopSessionMember.sendEvent(USERS_CONNECTED_EVENT, new UsersConnectedEvent(users, instanceId));
            });
        }
    }

    private synchronized void removeDesktopSession(final DesktopSession desktopSession) {
        this.desktopSessions.removeIf(item -> item.equals(desktopSession));
    }

    private synchronized void removeDesktopSessionMember(final DesktopSession desktopSession, final DesktopSessionMember desktopSessionMember) {
        desktopSession.removeMember(desktopSessionMember);
        if (desktopSession.getMembers().isEmpty()) {
            this.removeDesktopSession(desktopSession);
        }
    }

    private synchronized Optional<DesktopSession> getDesktopSession(final Long sessionId) {
        return this.desktopSessions.stream().filter(desktopSession -> desktopSession.getSessionId().equals(sessionId)).findAny();
    }

    private synchronized DesktopSession getOrCreateDesktopSession(final Long sessionId, final Long instanceId, final String protocol) {
        return this.desktopSessions.stream()
            .filter(desktopSession -> desktopSession.getSessionId().equals(sessionId))
            .findFirst()
            .orElseGet(() -> {
                DesktopSession desktopSession = new DesktopSession(sessionId, instanceId, protocol);
                this.desktopSessions.add(desktopSession);
                return desktopSession;
            });
    }

    private List<ConnectedUser> getConnectedUsers(final DesktopSession desktopSession) {
        final Long instanceId = desktopSession.getInstanceId();
        List<InstanceSessionMember> instanceSessionMembers = this.instanceSessionService.getAllSessionMembersByInstanceSessionId(desktopSession.getSessionId());
        logger.info("Instance {} has {} connected users", instanceId, instanceSessionMembers.size());
        return instanceSessionMembers.stream().map(instanceSessionMember -> {
            User user = instanceSessionMember.getUser();
            InstanceMemberRole role = InstanceMemberRole.valueOf(instanceSessionMember.getRole());
            if (desktopSession.isLocked() && role.equals(InstanceMemberRole.USER)) {
                role= InstanceMemberRole.GUEST;
            }
            return new ConnectedUser(user.getId(), user.getFullName(), role);
        }).toList();
    }
}
