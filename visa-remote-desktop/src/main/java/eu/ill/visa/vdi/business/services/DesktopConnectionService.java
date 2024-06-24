package eu.ill.visa.vdi.business.services;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.business.services.InstanceExpirationService;
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
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.DesktopConnection;
import eu.ill.visa.vdi.gateway.events.UserConnectedEvent;
import eu.ill.visa.vdi.gateway.events.UserDisconnectedEvent;
import eu.ill.visa.vdi.gateway.events.UsersConnectedEvent;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static eu.ill.visa.vdi.domain.models.Event.*;

@Startup
@ApplicationScoped
public class DesktopConnectionService {

    private final static String PROTOCOL_PARAMETER = "protocol";
    private final static Logger logger = LoggerFactory.getLogger(DesktopConnectionService.class);

    private final InstanceSessionService instanceSessionService;
    private final UserService userService;
    private final GuacamoleDesktopService guacamoleDesktopService;
    private final WebXDesktopService webXDesktopService;
    private final InstanceExpirationService instanceExpirationService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;

    private final RemoteDesktopBroker remoteDesktopBroker;

    private final List<DesktopConnection> desktopConnections = new ArrayList<>();

    @Inject
    public DesktopConnectionService(final InstanceSessionService instanceSessionService,
                                    final UserService userService,
                                    final GuacamoleDesktopService guacamoleDesktopService,
                                    final WebXDesktopService webXDesktopService,
                                    final InstanceExpirationService instanceExpirationService,
                                    final VirtualDesktopConfiguration virtualDesktopConfiguration,
                                    final jakarta.enterprise.inject.Instance<RemoteDesktopBroker> remoteDesktopBrokerInstance) {
        this.instanceSessionService = instanceSessionService;
        this.userService = userService;
        this.guacamoleDesktopService = guacamoleDesktopService;
        this.webXDesktopService = webXDesktopService;
        this.instanceExpirationService = instanceExpirationService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;

        this.remoteDesktopBroker = remoteDesktopBrokerInstance.get();

        this.remoteDesktopBroker.subscribe(UserConnectedMessage.class)
            .next((message) -> this.onUserConnected(message.instanceId(), message.user(), message.connectionId()));
        this.remoteDesktopBroker.subscribe(UserDisconnectedMessage.class)
            .next((message) -> this.onUserDisconnected(message.instanceId(), message.user(), message.connectionId()));
        this.remoteDesktopBroker.subscribe(AccessRevokedMessage.class)
            .next((message) -> this.onAccessRevoked(message.instanceId(), message.userId()));
        this.remoteDesktopBroker.subscribe(RoomClosedMessage.class)
            .next((message) -> this.onRoomClosed(message.instanceId()));
        this.remoteDesktopBroker.subscribe(RoomLockedMessage.class)
            .next((message) -> this.onRoomLocked(message.instanceId()));
        this.remoteDesktopBroker.subscribe(RoomUnlockedMessage.class)
            .next((message) -> this.onRoomUnlocked(message.instanceId()));
    }

    public DesktopConnection createDesktopConnection(final SocketIOClient client, final Instance instance, final ConnectedUser user) throws OwnerNotConnectedException, UnauthorizedException, ConnectionException {
        final InstanceMemberRole role = user.getRole();
        if (role == InstanceMemberRole.NONE) {
            throw new UnauthorizedException("User " + user.getFullName() + " is unauthorised to access the instance " + instance.getId());
        }

        final HandshakeData data = client.getHandshakeData();
        final String protocol = data.getSingleUrlParam(PROTOCOL_PARAMETER);

        boolean isWebX = protocol != null && protocol.equals("webx");
        final ConnectionThread thread;
        if (isWebX) {
            logger.info("User {} creating WebX desktop connection to instance {}", (user.getFullName() + " (" + role.toString() + ")"), instance.getId());
            thread = webXDesktopService.connect(client, instance, user);
        } else {
            logger.info("User {} creating Guacamole desktop connection to instance {}", (user.getFullName() + " (" + role.toString() + ")"), instance.getId());
            thread = guacamoleDesktopService.connect(client, instance, user);
        }

        DesktopConnection desktopConnection = new DesktopConnection(client, instance.getId(), instance.getLastSeenAt(), user, thread, instance.getId().toString());
        this.addDesktopConnection(desktopConnection);

        InstanceSession instanceSession = this.instanceSessionService.getByInstance(instance);
        boolean unlockRoom = virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)
            && role.equals(InstanceMemberRole.OWNER)
            && !this.isOwnerConnected(instance);

        // Update the connected clients of the session
        this.instanceSessionService.addInstanceSessionMember(instanceSession, client.getSessionId(), this.userService.getById(user.getId()), role.toString());

        // Remove instance from instance_expiration table if it is there due to inactivity
        this.instanceExpirationService.onInstanceActivated(instanceSession.getInstance());

        if (unlockRoom) {
            // Unlock room for all clients
            this.unlockRoom(instance);

        } else {
            this.connectUser(instance, this.getConnectedUser(client),desktopConnection.getConnectionId());
        }

        return desktopConnection;
    }

    private synchronized void addDesktopConnection(final DesktopConnection desktopConnection) {
        this.desktopConnections.add(desktopConnection);
    }

    public synchronized DesktopConnection getDesktopConnection(final SocketIOClient client) {
        String clientId = client.getSessionId().toString();
        return this.desktopConnections.stream().filter(desktopConnection -> desktopConnection.getConnectionId().equals(clientId)).findAny().orElse(null);
    }

    public synchronized List<DesktopConnection> getDesktopConnectionsForInstanceId(Long instanceId) {
        return this.desktopConnections.stream()
            .filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId))
            .toList();
    }

    public synchronized List<DesktopConnection> getOwnerDesktopConnectionsForInstanceId(Long instanceId) {
        return this.desktopConnections.stream()
            .filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId))
            .filter(desktopConnection -> desktopConnection.getConnectedUser().getRole().equals(InstanceMemberRole.OWNER))
            .toList();
    }

    public synchronized List<DesktopConnection> getNonOwnerDesktopConnectionsForInstanceId(Long instanceId) {
        return this.desktopConnections.stream()
            .filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId))
            .filter(desktopConnection -> !desktopConnection.getConnectedUser().getRole().equals(InstanceMemberRole.OWNER))
            .toList();
    }

    public synchronized List<DesktopConnection> getDesktopConnectionsForInstanceIdAndUserId(Long instanceId, final String userId) {
        return this.desktopConnections.stream()
            .filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId))
            .filter(desktopConnection -> desktopConnection.getConnectedUser().getId().equals(userId))
            .toList();
    }

    public synchronized void removeDesktopConnection(final SocketIOClient client) {
        DesktopConnection desktopConnection = this.getDesktopConnection(client);
        if (desktopConnection != null) {
            this.desktopConnections.remove(desktopConnection);
        }
    }

    public ConnectedUser getConnectedUser(final SocketIOClient client) {
        final DesktopConnection desktopConnection = this.getDesktopConnection(client);
        return desktopConnection.getConnectedUser();
    }

    public List<ConnectedUser> getConnectedUsers(Long instanceId, boolean isRoomLocked) {
        List<InstanceSessionMember> instanceSessionMembers = this.instanceSessionService.getAllSessionMembersByInstanceId(instanceId);
        logger.info("Instance {} has {} connected users", instanceId, instanceSessionMembers.size());
        return instanceSessionMembers.stream().map(instanceSessionMember -> {
            User user = instanceSessionMember.getUser();
            InstanceMemberRole role = InstanceMemberRole.valueOf(instanceSessionMember.getRole());
            if (isRoomLocked && role.equals(InstanceMemberRole.USER)) {
                role= InstanceMemberRole.GUEST;
            }
            return new ConnectedUser(user.getId(), user.getFullName(), role);
        }).toList();
    }

    public boolean isOwnerConnected(final Instance instance) {
        List<InstanceSessionMember> instanceSessionMembers = this.instanceSessionService.getAllSessionMembers(instance);
        for (final InstanceSessionMember instanceSessionMember : instanceSessionMembers) {
            String role = instanceSessionMember.getRole();
            if (role.equals("OWNER")) {
                return true;
            }
        }

        return false;
    }

    public void connectUser(final Instance instance, final ConnectedUser user, final String connectionId) {
        this.remoteDesktopBroker.broadcast(new UserConnectedMessage(instance.getId(), user, connectionId));
    }

    public void disconnectUser(final Instance instance, final ConnectedUser user, final String connectionId) {
        this.remoteDesktopBroker.broadcast(new UserDisconnectedMessage(instance.getId(), user, connectionId));
    }

    public void revokeUserAccess(final SocketIOClient client, final String userId) {
        final DesktopConnection connection = this.getDesktopConnection(client);

        // Verify that we have a connection and that the user is the owner
        if (connection != null && connection.getConnectedUser().getRole().equals(InstanceMemberRole.OWNER)) {
            this.remoteDesktopBroker.broadcast(new AccessRevokedMessage(connection.getInstanceId(), userId));
        }
    }

    public void closeRoom(final Instance instance) {
        this.remoteDesktopBroker.broadcast(new RoomClosedMessage(instance.getId()));
    }

    public void lockRoom(final Instance instance) {
        this.remoteDesktopBroker.broadcast(new RoomLockedMessage(instance.getId()));
    }

    public void unlockRoom(final Instance instance) {
        this.remoteDesktopBroker.broadcast(new RoomUnlockedMessage(instance.getId()));
    }

    public void onUserConnected(final Long instanceId, final ConnectedUser user, final String connectionId) {
        logger.info("User {} connected to instance {}", user, instanceId);

        List<DesktopConnection> instanceConnections = this.getDesktopConnectionsForInstanceId(instanceId);
        instanceConnections.forEach(desktopConnection -> {
            if (!desktopConnection.getConnectionId().equals(connectionId)) {
                desktopConnection.getClient().sendEvent(USER_CONNECTED_EVENT, new UserConnectedEvent(user, instanceId));
            }
        });

        List<ConnectedUser> users = this.getConnectedUsers(instanceId, false);
        if (!users.isEmpty()) {
            logger.info("Instance {} has the following users connected: {}", instanceId, users.stream().map(ConnectedUser::toString).toList());
            instanceConnections.forEach(desktopConnection -> {
                desktopConnection.getClient().sendEvent(USERS_CONNECTED_EVENT, new UsersConnectedEvent(users, instanceId));
            });
        }
    }

    public void onUserDisconnected(final Long instanceId, final ConnectedUser user, final String connectionId) {
        logger.info("User {} disconnected from instance {}", user, instanceId);

        List<DesktopConnection> instanceConnections = this.getDesktopConnectionsForInstanceId(instanceId);
        instanceConnections.forEach(desktopConnection -> {
            if (!desktopConnection.getConnectionId().equals(connectionId)) {
                desktopConnection.getClient().sendEvent(USER_DISCONNECTED_EVENT, new UserDisconnectedEvent(user, instanceId));
            }
        });

        List<ConnectedUser> users = this.getConnectedUsers(instanceId, false);
        if (!users.isEmpty()) {
            logger.info("Instance {} has the following users connected: {}", instanceId, users.stream().map(ConnectedUser::toString).toList());
            instanceConnections.forEach(desktopConnection -> {
                desktopConnection.getClient().sendEvent(USERS_CONNECTED_EVENT, new UsersConnectedEvent(users, instanceId));
            });
        }
    }

    public void onAccessRevoked(final Long instanceId, final String userId) {
        logger.info("Access revoked for user with id: {} in instance {}", userId, instanceId);

        this.getDesktopConnectionsForInstanceIdAndUserId(instanceId, userId).forEach(desktopConnection -> {
            SocketIOClient client = desktopConnection.getClient();
            logger.info("Revoking access to remote desktop for instance {} for user {} with connection ID {}", instanceId, desktopConnection.getConnectedUser().getFullName(), desktopConnection.getConnectionId());
            client.sendEvent(ACCESS_REVOKED_EVENT);
            client.disconnect();
        });
    }

    public void onRoomClosed(final Long instanceId) {
        logger.info("Room closed (owner away) for instance {}: disconnecting all clients", instanceId);

        this.getNonOwnerDesktopConnectionsForInstanceId(instanceId).forEach(desktopConnection -> {
            SocketIOClient client = desktopConnection.getClient();
            client.sendEvent(OWNER_AWAY_EVENT);
            client.disconnect();
        });
    }

    public void onRoomLocked(final Long instanceId) {
        logger.info("Room locked (owner away) for instance {}: making all clients read-only", instanceId);

        this.getNonOwnerDesktopConnectionsForInstanceId(instanceId).stream().forEach(desktopConnection -> {
            desktopConnection.setRoomLocked(true);

            desktopConnection.getClient().sendEvent(ROOM_LOCKED_EVENT);
        });
    }

    public void onRoomUnlocked(final Long instanceId) {
        logger.info("Room unlocked (owner back) for instance {}: all clients resume original roles", instanceId);

        this.getNonOwnerDesktopConnectionsForInstanceId(instanceId).forEach(desktopConnection -> {
            desktopConnection.setRoomLocked(false);

            desktopConnection.getClient().sendEvent(ROOM_UNLOCKED_EVENT);
        });
    }
}
