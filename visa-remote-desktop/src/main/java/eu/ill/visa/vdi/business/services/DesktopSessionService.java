package eu.ill.visa.vdi.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.business.services.*;
import eu.ill.visa.core.domain.fetches.InstanceFetch;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.partial.InstancePartial;
import eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.broker.*;
import eu.ill.visa.vdi.business.concurrency.ConnectionThreadExecutor;
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

import java.util.*;

import static eu.ill.visa.vdi.domain.models.SessionEvent.*;

@Startup
@ApplicationScoped
public class DesktopSessionService {

    private final static Logger logger = LoggerFactory.getLogger(DesktopSessionService.class);

    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceSessionMemberService instanceSessionMemberService;
    private final InstanceActivityService instanceActivityService;
    private final UserService userService;
    private final GuacamoleDesktopService guacamoleDesktopService;
    private final WebXDesktopService webXDesktopService;
    private final InstanceExpirationService instanceExpirationService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;
    private final EventDispatcher eventDispatcher;
    private final ConnectionThreadExecutor connectionThreadExecutor;

    private final MessageBroker messageBroker;

    private final List<DesktopSession> desktopSessions = new ArrayList<>();
    private final Map<String, DesktopSessionMember> desktopSessionMembers = new HashMap<>();

    @Inject
    public DesktopSessionService(final InstanceService instanceService,
                                 final InstanceSessionService instanceSessionService,
                                 final InstanceSessionMemberService instanceSessionMemberService,
                                 final InstanceActivityService instanceActivityService,
                                 final UserService userService,
                                 final GuacamoleDesktopService guacamoleDesktopService,
                                 final WebXDesktopService webXDesktopService,
                                 final InstanceExpirationService instanceExpirationService,
                                 final VirtualDesktopConfiguration virtualDesktopConfiguration,
                                 final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance,
                                 final EventDispatcher eventDispatcher,
                                 final ConnectionThreadExecutor connectionThreadExecutor) {
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.instanceSessionMemberService = instanceSessionMemberService;
        this.instanceActivityService = instanceActivityService;
        this.userService = userService;
        this.guacamoleDesktopService = guacamoleDesktopService;
        this.webXDesktopService = webXDesktopService;
        this.instanceExpirationService = instanceExpirationService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
        this.messageBroker = messageBrokerInstance.get();
        this.eventDispatcher = eventDispatcher;
        this.connectionThreadExecutor = connectionThreadExecutor;

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

    public DesktopSessionMember createDesktopSessionMember(final SocketClient client, final ConnectedUser user, final Instance instance, final NopSender nopSender) throws OwnerNotConnectedException, UnauthorizedException, ConnectionException {

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
        final InstanceSession instanceSession = this.instanceSessionService.getLatestByInstanceAndProtocol(instance, client.protocol());
        DesktopSession desktopSession = this.getOrCreateDesktopSession(instanceSession.getId(), instance.getId(), client.protocol());

        // Create session member: Add a NOP timer to keep the connection alive
        DesktopSessionMember desktopSessionMember = new DesktopSessionMember(client.clientId(), user, remoteDesktopConnection, desktopSession, nopSender);
        this.addDesktopSessionMember(desktopSession, desktopSessionMember);

        // Activate idle session timer
        desktopSessionMember.idleSessionHandler().start(() -> this.onDesktopMemberIdle(desktopSessionMember));

        // Start the connection thread
        this.connectionThreadExecutor.startConnectionThread(remoteDesktopConnection.getConnectionThread());

        boolean unlockSession = virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)
            && role.equals(InstanceMemberRole.OWNER)
            && !this.isOwnerConnected(instanceSession.getId());

        // Update the connected clients of the session
        this.instanceSessionMemberService.create(instanceSession, desktopSessionMember.clientId(), this.userService.getById(user.getId()), role);

        // Remove instance from instance_expiration table if it is there due to inactivity
        this.instanceExpirationService.onInstanceActivated(instance);

        if (unlockSession) {
            // Unlock session for all clients
            this.unlockSession(desktopSession.getSessionId());

        } else {
            this.connectUser(desktopSession.getSessionId(), desktopSessionMember.clientId(), user);
        }

        logger.info("After connection, currently have {} remote desktop clients (Guacamole = {}, WebX = {})",
            this.countSessionMembers(null),
            this.countSessionMembers(DesktopService.GUACAMOLE_PROTOCOL),
            this.countSessionMembers(DesktopService.WEBX_PROTOCOL));

        return desktopSessionMember;
    }

    public void onDesktopMemberDisconnect(final DesktopSessionMember desktopSessionMember) {
        // Stop the idle handler
        desktopSessionMember.idleSessionHandler().stop();
        desktopSessionMember.nopTimer().cancel();

        desktopSessionMember.remoteDesktopConnection().getConnectionThread().closeTunnel();

        DesktopSession desktopSession = desktopSessionMember.session();
        final Long sessionId = desktopSession.getSessionId();

        this.removeDesktopSessionMember(desktopSession, desktopSessionMember);

        InstanceSession session = instanceSessionService.getById(sessionId);
        if (session != null) {
            // Remove client/member from instance session
            instanceSessionService.deleteSessionMember(session, desktopSessionMember.clientId());

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

        logger.info("After disconnection, currently have {} remote desktop clients (Guacamole = {}, WebX = {})",
            this.countSessionMembers(null),
            this.countSessionMembers(DesktopService.GUACAMOLE_PROTOCOL),
            this.countSessionMembers(DesktopService.WEBX_PROTOCOL));

    }

    private void onDesktopMemberIdle(final DesktopSessionMember desktopSessionMember) {
        logger.warn("Idle timeout for desktop session member {}: disconnecting", desktopSessionMember);
        this.onDesktopMemberDisconnect(desktopSessionMember);
    }

    public boolean isOwnerConnected(final Long sessionId) {
        List<InstanceSessionMemberPartial> instanceSessionMembers = this.instanceSessionMemberService.getAllPartialsByInstanceSessionId(sessionId);
        return instanceSessionMembers.stream().anyMatch(instanceSessionMember -> instanceSessionMember.getRole().equals(InstanceMemberRole.OWNER));
    }

    private void connectUser(final Long sessionId, final String clientId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserConnectedMessage(sessionId, clientId, user));
    }

    private void disconnectUser(final Long sessionId, final String clientId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserDisconnectedMessage(sessionId, clientId, user));
    }

    public void revokeUserAccess(final DesktopSessionMember ownerSessionMember, final String userId) {
        // Verify that we have a connection and that the user is the owner
        if (ownerSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER)) {
            // Forward reply to broker once we've verified that the revoke command comes from the owner
            this.messageBroker.broadcast(new AccessRevokedMessage(ownerSessionMember.session().getSessionId(), userId));
        }
    }

    private void closeSession(final Long sessionId) {
        this.messageBroker.broadcast(new SessionClosedMessage(sessionId));
    }

    private void lockSession(final Long sessionId, final ConnectedUser user) {
        this.messageBroker.broadcast(new SessionLockedMessage(sessionId, user));
    }

    private void unlockSession(final Long sessionId) {
        this.messageBroker.broadcast(new SessionUnlockedMessage(sessionId));
    }

    public Optional<DesktopSession> findDesktopSession(final Long sessionId) {
        synchronized (this.desktopSessions) {
            return this.desktopSessions.stream()
                .filter(desktopSession -> desktopSession.getSessionId().equals(sessionId))
                .findAny();
        }
    }

    public Optional<DesktopSessionMember> getDesktopSessionMember(final String clientId) {
        // Un-synchronized access to the map is ok since we are only reading from it and we want to go fast
        return Optional.ofNullable(this.desktopSessionMembers.get(clientId));
    }

    public void updateSessionMemberActivity(final DesktopSessionMember desktopSessionMember) {
        final DesktopSession desktopSession = desktopSessionMember.session();
        final RemoteDesktopConnection remoteDesktopConnection = desktopSessionMember.remoteDesktopConnection();
        final Long instanceSessionId = desktopSession.getSessionId();
        final String clientId = desktopSessionMember.clientId();

        final InstancePartial instance = this.instanceService.getPartialById(desktopSessionMember.session().getInstanceId());
        if (instance != null) {
            // Update instance
            instance.setLastSeenAt(remoteDesktopConnection.getLastInstanceUpdateTime());
            instance.setLastInteractionAt(remoteDesktopConnection.getLastInteractionAt());
            instanceService.updatePartial(instance);

            final InstanceSessionMemberPartial instanceSessionMember = this.instanceSessionMemberService.getPartialByInstanceSessionIdAndClientId(instanceSessionId, clientId);
            if (instanceSessionMember != null) {
                instanceSessionMember.setLastInteractionAt(remoteDesktopConnection.getLastInteractionAt());
                instanceSessionMemberService.updateInteractionAt(instanceSessionMember);

                InstanceActivityType instanceActivityType = remoteDesktopConnection.getInstanceActivity();
                if (instanceActivityType != null) {
                    this.instanceActivityService.create(instanceSessionMember.getUserId(), instanceSessionMember.getInstanceId(), instanceActivityType);
                    remoteDesktopConnection.resetInstanceActivity();
                }
            }
        }
    }

    private int countSessionMembers(String protocol) {
        synchronized (this.desktopSessions) {
            if (protocol == null) {
                return this.desktopSessions.stream()
                    .mapToInt(DesktopSession::memberCount)
                    .sum();

            } else {
                return this.desktopSessions.stream()
                    .filter(session -> session.getProtocol().equals(protocol))
                    .mapToInt(DesktopSession::memberCount)
                    .sum();
            }
        }
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
                    final Instance instance = this.instanceService.getById(desktopSession.getInstanceId(), List.of(InstanceFetch.members));
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

    private void removeDesktopSession(final DesktopSession desktopSession) {
        synchronized (this.desktopSessionMembers) {
            this.desktopSessions.removeIf(item -> item.equals(desktopSession));
        }
    }

    private void addDesktopSessionMember(final DesktopSession desktopSession, final DesktopSessionMember desktopSessionMember) {
        synchronized (this.desktopSessionMembers) {
            desktopSession.addMember(desktopSessionMember);
            this.desktopSessionMembers.put(desktopSessionMember.clientId(), desktopSessionMember);
        }
    }

    private void removeDesktopSessionMember(final DesktopSession desktopSession, final DesktopSessionMember desktopSessionMember) {
        synchronized (this.desktopSessionMembers) {
            this.desktopSessionMembers.remove(desktopSessionMember.clientId());
            desktopSession.removeMember(desktopSessionMember);
            if (desktopSession.getMembers().isEmpty()) {
                this.removeDesktopSession(desktopSession);
            }
        }
    }

    private Optional<DesktopSession> getDesktopSession(final Long sessionId) {
        synchronized (this.desktopSessions) {
            return this.desktopSessions.stream().filter(desktopSession -> desktopSession.getSessionId().equals(sessionId)).findAny();
        }
    }

    private DesktopSession getOrCreateDesktopSession(final Long sessionId, final Long instanceId, final String protocol) {
        synchronized (this.desktopSessions) {
            return this.desktopSessions.stream()
                .filter(desktopSession -> desktopSession.getSessionId().equals(sessionId))
                .findFirst()
                .orElseGet(() -> {
                    DesktopSession desktopSession = new DesktopSession(sessionId, instanceId, protocol);
                    this.desktopSessions.add(desktopSession);
                    return desktopSession;
                });
        }
    }

    private List<ConnectedUser> getConnectedUsers(final DesktopSession desktopSession) {
        final Long instanceId = desktopSession.getInstanceId();
        List<InstanceSessionMemberPartial> instanceSessionMembers = this.instanceSessionMemberService.getAllPartialsByInstanceSessionId(desktopSession.getSessionId());
        logger.info("Instance {} has {} connected users", instanceId, instanceSessionMembers.size());
        return instanceSessionMembers.stream().map(instanceSessionMember -> {
            InstanceMemberRole role = instanceSessionMember.getRole();
            if (desktopSession.isLocked() && role.equals(InstanceMemberRole.USER)) {
                role= InstanceMemberRole.GUEST;
            }
            return new ConnectedUser(instanceSessionMember.getUserId(), instanceSessionMember.getUserFullName(), role);
        }).toList();
    }
}
