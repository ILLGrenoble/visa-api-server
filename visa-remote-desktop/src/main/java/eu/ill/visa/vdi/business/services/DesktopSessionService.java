package eu.ill.visa.vdi.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.MessageBroker;
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
import eu.ill.visa.vdi.broker.*;
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
    private final EventDispatcher eventDispatcher;

    private final MessageBroker messageBroker;

    private final List<DesktopSession> desktopSessions = new ArrayList<>();

    @Inject
    public DesktopSessionService(final InstanceService instanceService,
                                 final InstanceSessionService instanceSessionService,
                                 final UserService userService,
                                 final GuacamoleDesktopService guacamoleDesktopService,
                                 final WebXDesktopService webXDesktopService,
                                 final InstanceExpirationService instanceExpirationService,
                                 final VirtualDesktopConfiguration virtualDesktopConfiguration,
                                 final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance,
                                 final EventDispatcher eventDispatcher) {
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.userService = userService;
        this.guacamoleDesktopService = guacamoleDesktopService;
        this.webXDesktopService = webXDesktopService;
        this.instanceExpirationService = instanceExpirationService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
        this.messageBroker = messageBrokerInstance.get();
        this.eventDispatcher = eventDispatcher;

        this.messageBroker.subscribe(UserConnectedMessage.class)
            .next((message) -> this.onUserConnected(message.sessionId(), message.clientId(), message.user()));
        this.messageBroker.subscribe(UserDisconnectedMessage.class)
            .next((message) -> this.onUserDisconnected(message.sessionId(), message.clientId(), message.user()));
        this.messageBroker.subscribe(AccessRevokedMessage.class)
            .next((message) -> this.onAccessRevoked(message.sessionId(), message.userId()));
        this.messageBroker.subscribe(SessionClosedMessage.class)
            .next((message) -> this.onSessionClosed(message.sessionId()));
        this.messageBroker.subscribe(SessionLockedMessage.class)
            .next((message) -> this.onSessionLocked(message.sessionId(), message.user()));
        this.messageBroker.subscribe(SessionUnlockedMessage.class)
            .next((message) -> this.onSessionUnlocked(message.sessionId()));
    }

    @Shutdown
    public void shutdown() {
        this.messageBroker.shutdown();
    }

    public synchronized DesktopSessionMember createDesktopSessionMember(final SocketClient client, final ConnectedUser user, final Instance instance) throws OwnerNotConnectedException, UnauthorizedException, ConnectionException {

        final InstanceMemberRole role = user.getRole();
        if (role == InstanceMemberRole.NONE) {
            throw new UnauthorizedException("User " + user.getFullName() + " is unauthorised to access the instance " + instance.getId());
        }

        // Create RemoteDesktopConnection (guacamole or webx)
        boolean isWebX = client.protocol().equals(DesktopService.WEBX_PROTOCOL);
        final RemoteDesktopConnection remoteDesktopConnection;
        if (isWebX) {
            logger.info("User {} creating WebX desktop connection to instance {}", (user.getFullName() + " (" + role.toString() + ")"), instance.getId());
            remoteDesktopConnection = webXDesktopService.connect(client, instance, user);
        } else {
            logger.info("User {} creating Guacamole desktop connection to instance {}", (user.getFullName() + " (" + role.toString() + ")"), instance.getId());
            remoteDesktopConnection = guacamoleDesktopService.connect(client, instance, user);
        }

        // Get or create a new DesktopSession: use the ID of the InstanceSession
        final InstanceSession instanceSession = this.instanceSessionService.getByInstanceAndProtocol(instance, client.protocol());
        DesktopSession desktopSession = this.getOrCreateDesktopSession(instanceSession.getId(), instance.getId(), client.protocol());

        // Create session member
        DesktopSessionMember desktopSessionMember = new DesktopSessionMember(client.clientId(), user, remoteDesktopConnection, desktopSession);
        desktopSession.addMember(desktopSessionMember);

        boolean unlockSession = virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)
            && role.equals(InstanceMemberRole.OWNER)
            && !this.isOwnerConnected(instanceSession.getId());

        // Update the connected clients of the session
        this.instanceSessionService.addInstanceSessionMember(instanceSession, desktopSessionMember.clientId(), this.userService.getById(user.getId()), role.toString());

        // Remove instance from instance_expiration table if it is there due to inactivity
        this.instanceExpirationService.onInstanceActivated(instanceSession.getInstance());

        if (unlockSession) {
            // Unlock session for all clients
            this.unlockSession(desktopSession.getSessionId());

        } else {
            this.connectUser(desktopSession.getSessionId(), desktopSessionMember.clientId(), user);
        }

        return desktopSessionMember;
    }

    public synchronized void onDesktopMemberDisconnect(final DesktopSessionMember desktopSessionMember) {
        desktopSessionMember.remoteDesktopConnection().getConnectionThread().closeTunnel();

        DesktopSession desktopSession = desktopSessionMember.session();
        final Long sessionId = desktopSession.getSessionId();

        this.removeDesktopSessionMember(desktopSession, desktopSessionMember);

        InstanceSession session = instanceSessionService.getById(sessionId);
        if (session != null) {
            // Remove client/member from instance session
            instanceSessionService.removeInstanceSessionMember(session, desktopSessionMember.clientId());

            if (desktopSessionMember.isRole(InstanceMemberRole.OWNER) && !this.isOwnerConnected(sessionId)) {
                if (this.virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)) {
                    this.lockSession(sessionId, desktopSessionMember.connectedUser());

                } else {
                    logger.info("There is no owner ar admin connected so all clients will be disconnected");
                    this.closeSession(sessionId);
                }

            } else {
                // broadcast events for a user disconnected and current users
                this.disconnectUser(sessionId, desktopSessionMember.clientId(), desktopSessionMember.connectedUser());
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

    public void connectUser(final Long sessionId, final String clientId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserConnectedMessage(sessionId, clientId, user));
    }

    public void disconnectUser(final Long sessionId, final String clientId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserDisconnectedMessage(sessionId, clientId, user));
    }

    public void revokeUserAccess(final DesktopSessionMember ownerSessionMember, final String userId) {
        // Verify that we have a connection and that the user is the owner
        if (ownerSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER)) {
            // Forward reply to broker once we've verified that the revoke command comes from the owner
            this.messageBroker.broadcast(new AccessRevokedMessage(ownerSessionMember.session().getSessionId(), userId));
        }
    }

    public void closeSession(final Long sessionId) {
        this.messageBroker.broadcast(new SessionClosedMessage(sessionId));
    }

    public void lockSession(final Long sessionId, final ConnectedUser user) {
        this.messageBroker.broadcast(new SessionLockedMessage(sessionId, user));
    }

    public void unlockSession(final Long sessionId) {
        this.messageBroker.broadcast(new SessionUnlockedMessage(sessionId));
    }

    public synchronized Optional<DesktopSession> findDesktopSession(final Long sessionId) {
        return this.desktopSessions.stream()
            .filter(desktopSession -> desktopSession.getSessionId().equals(sessionId))
            .findAny();
    }

    public synchronized Optional<DesktopSessionMember> findDesktopSessionMemberByClientId(final String clientId) {
        return this.desktopSessions.stream()
            .flatMap(desktopSession -> desktopSession.getMembers().stream())
            .filter(desktopSessionMember -> desktopSessionMember.clientId().equals(clientId))
            .findAny();
    }

    private void onUserConnected(final Long sessionId, final String clientId, final ConnectedUser user) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstanceId();
            final String protocol = desktopSession.getProtocol();
            logger.info("User {} connected to instance {} with protocol {}", user, instanceId, protocol);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.clientId().equals(clientId))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), USER_CONNECTED_EVENT, new UserConnectedEvent(user, instanceId));
                });

            this.sendUsersConnectedEvent(desktopSession);
        });
    }

    private void onUserDisconnected(final Long sessionId, final String clientId, final ConnectedUser user) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstanceId();
            final String protocol = desktopSession.getProtocol();
            logger.info("User {} disconnected from instance {} with protocol {}", user, instanceId, protocol);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.clientId().equals(clientId))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), USER_DISCONNECTED_EVENT, new UserDisconnectedEvent(user, instanceId));
                });

            this.sendUsersConnectedEvent(desktopSession);
        });
    }

    private void onAccessRevoked(final Long sessionId, final String userId) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstanceId();
            logger.info("Access revoked for user with id: {} in instance {}", userId, instanceId);

            this.eventDispatcher.sendEventToUser(userId, ACCESS_REVOKED_EVENT);

            desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().getId().equals(userId))
                .forEach(desktopSessionMember -> {
                    logger.info("Revoking access to remote desktop for instance {} with protocol {} for user {} with client ID {}", instanceId, desktopSession.getProtocol(), desktopSessionMember.connectedUser().getFullName(), desktopSessionMember.clientId());
                    desktopSessionMember.disconnect();
                });
        });
    }

    private void onSessionClosed(final Long sessionId) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            logger.info("Session closed (owner away) for instance {} with protocol {}: disconnecting all clients", desktopSession.getInstanceId(), desktopSession.getProtocol());

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), ACCESS_REVOKED_EVENT);
                    desktopSessionMember.disconnect();
                });
        });
    }

    private void onSessionLocked(final Long sessionId, final ConnectedUser user) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            logger.info("Session locked (owner away) for instance {} with protocol {}: making all clients read-only", desktopSession.getInstanceId(), desktopSession.getProtocol());
            desktopSession.setLocked(true);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    final Instance instance = this.instanceService.getFullById(desktopSession.getInstanceId());
                    if (desktopSessionMember.connectedUser().isRole(InstanceMemberRole.SUPPORT) && instanceSessionService.canConnectWhileOwnerAway(instance, desktopSessionMember.connectedUser().getId())) {
                        this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), USER_DISCONNECTED_EVENT, new UserDisconnectedEvent(user, desktopSession.getInstanceId()));

                    } else {
                        this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), SESSION_LOCKED_EVENT);
                    }

                });

            this.sendUsersConnectedEvent(desktopSession);
        });
    }

    private void onSessionUnlocked(final Long sessionId) {
        this.getDesktopSession(sessionId).ifPresent(desktopSession -> {
            logger.info("Session unlocked (owner back) for instance {} with protocol {}: all clients resume original roles", desktopSession.getInstanceId(), desktopSession.getProtocol());
            desktopSession.setLocked(false);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), SESSION_UNLOCKED_EVENT);
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
                this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), USERS_CONNECTED_EVENT, new UsersConnectedEvent(users, instanceId));
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
