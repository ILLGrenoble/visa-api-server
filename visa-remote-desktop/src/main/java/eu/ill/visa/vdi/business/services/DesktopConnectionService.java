package eu.ill.visa.vdi.business.services;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import eu.ill.visa.business.services.InstanceExpirationService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.RemoteDesktopPubSub;
import eu.ill.visa.vdi.brokers.messages.AccessRevokedMessage;
import eu.ill.visa.vdi.brokers.messages.RoomClosedMessage;
import eu.ill.visa.vdi.brokers.messages.RoomLockedMessage;
import eu.ill.visa.vdi.brokers.messages.RoomUnlockedMessage;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.domain.events.Event;
import eu.ill.visa.vdi.domain.events.UserConnectedEvent;
import eu.ill.visa.vdi.domain.events.UsersConnectedEvent;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.DesktopConnection;
import eu.ill.visa.vdi.domain.models.Role;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static eu.ill.visa.vdi.domain.events.Event.*;

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

    private final List<DesktopConnection> desktopConnections = new ArrayList<>();

    private final RemoteDesktopPubSub<AccessRevokedMessage> accessRevokedPubSub;
    private final RemoteDesktopPubSub<RoomClosedMessage> roomClosedPubSub;
    private final RemoteDesktopPubSub<RoomLockedMessage> roomLockedPubSub;
    private final RemoteDesktopPubSub<RoomUnlockedMessage> roomUnlockedPubSub;

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

        RemoteDesktopBroker remoteDesktopBroker = remoteDesktopBrokerInstance.get();

        this.accessRevokedPubSub = remoteDesktopBroker.createPubSub(AccessRevokedMessage.class,
            (message) -> this.onAccessRevoked(message.instanceId(), message.userId()));

        this.roomClosedPubSub = remoteDesktopBroker.createPubSub(RoomClosedMessage.class,
            (message) -> this.onRoomClosed(message.instanceId()));

        this.roomLockedPubSub = remoteDesktopBroker.createPubSub(RoomLockedMessage.class,
            (message) -> this.onRoomLocked(message.instanceId()));

        this.roomUnlockedPubSub = remoteDesktopBroker.createPubSub(RoomUnlockedMessage.class,
            (message) -> this.onRoomUnlocked(message.instanceId()));
    }

    public void broadcast(final SocketIOClient client, final Event...events) {
        final DesktopConnection connection = this.getDesktopConnection(client);
        this.broadcast(client, connection.getRoomId(), events);
    }

    public void broadcast(final SocketIOClient client, String roomId, final Event ...events) {
        final SocketIONamespace namespace = client.getNamespace();
        final BroadcastOperations operations = namespace.getRoomOperations(roomId);

        for (Event event : events) {
            event.broadcast(client, operations);
        }
    }

    public DesktopConnection createDesktopConnection(final SocketIOClient client, final Instance instance, final ConnectedUser user) throws OwnerNotConnectedException, UnauthorizedException, ConnectionException {
        final Role role = user.getRole();
        if (role == Role.NONE) {
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

        client.joinRoom(desktopConnection.getRoomId());

        InstanceSession instanceSession = this.instanceSessionService.getByInstance(instance);
        boolean unlockRoom = virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)
            && role.equals(Role.OWNER)
            && !this.isOwnerConnected(instance);

        // Update the connected clients of the session
        this.instanceSessionService.addInstanceSessionMember(instanceSession, client.getSessionId(), this.userService.getById(user.getId()), role.toString());

        // Remove instance from instance_expiration table if it is there due to inactivity
        this.instanceExpirationService.onInstanceActivated(instanceSession.getInstance());

        if (unlockRoom) {
            // Unlock room for all clients
            this.unlockRoom(instance);

        } else {
            this.broadcast(client,
                new UserConnectedEvent(this.getConnectedUser(client), client.getSessionId().toString()),
                new UsersConnectedEvent(instance, this.getConnectedUsers(instance, false))
            );
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

    public synchronized List<DesktopConnection> getOwnerDesktopConnectionsForInstanceId(Long instanceId) {
        return this.desktopConnections.stream()
            .filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId))
            .filter(desktopConnection -> desktopConnection.getConnectedUser().getRole().equals(Role.OWNER))
            .toList();
    }

    public synchronized List<DesktopConnection> getNonOwnerDesktopConnectionsForInstanceId(Long instanceId) {
        return this.desktopConnections.stream()
            .filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId))
            .filter(desktopConnection -> !desktopConnection.getConnectedUser().getRole().equals(Role.OWNER))
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

    public List<ConnectedUser> getConnectedUsers(Instance instance, boolean isRoomLocked) {
        List<InstanceSessionMember> instanceSessionMembers = this.instanceSessionService.getAllSessionMembers(instance);
        logger.info("Instance {} has {} connected users", instance.getId(), instanceSessionMembers.size());
        return instanceSessionMembers.stream().map(instanceSessionMember -> {
            User user = instanceSessionMember.getUser();
            Role role = Role.valueOf(instanceSessionMember.getRole());
            if (isRoomLocked && role.equals(Role.USER)) {
                role= Role.GUEST;
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

    public void revokeUserAccess(final SocketIOClient client, final String userId) {
        final DesktopConnection connection = this.getDesktopConnection(client);

        // Verify that we have a connection and that the user is the owner
        if (connection != null && connection.getConnectedUser().getRole().equals(Role.OWNER)) {
            this.accessRevokedPubSub.broadcast(new AccessRevokedMessage(connection.getInstanceId(), userId));
        }
    }

    public void closeRoom(final Instance instance) {
        this.roomClosedPubSub.broadcast(new RoomClosedMessage(instance.getId()));
    }

    public void lockRoom(final Instance instance) {
        this.roomLockedPubSub.broadcast(new RoomLockedMessage(instance.getId()));
    }

    public void unlockRoom(final Instance instance) {
        this.roomUnlockedPubSub.broadcast(new RoomUnlockedMessage(instance.getId()));
    }

    public void onAccessRevoked(Long instanceId, final String userId) {
        this.getDesktopConnectionsForInstanceIdAndUserId(instanceId, userId).forEach(desktopConnection -> {
            SocketIOClient client = desktopConnection.getClient();
            logger.info("Revoking access to remote desktop for instance {} for user {} with connection ID {}", instanceId, desktopConnection.getConnectedUser().getFullName(), desktopConnection.getConnectionId());
            client.sendEvent(ACCESS_REVOKED_EVENT);
            client.disconnect();
        });
    }

    public void onRoomClosed(final Long instanceId) {
        this.getNonOwnerDesktopConnectionsForInstanceId(instanceId).forEach(desktopConnection -> {
            SocketIOClient client = desktopConnection.getClient();
            client.sendEvent(OWNER_AWAY_EVENT);
            client.disconnect();
        });
    }

    public void onRoomLocked(final Long instanceId) {
        this.getNonOwnerDesktopConnectionsForInstanceId(instanceId).stream().forEach(desktopConnection -> {
            desktopConnection.setRoomLocked(true);

            desktopConnection.getClient().sendEvent(ROOM_LOCKED_EVENT);
        });
    }

    public void onRoomUnlocked(final Long instanceId) {
        this.getNonOwnerDesktopConnectionsForInstanceId(instanceId).forEach(desktopConnection -> {
            desktopConnection.setRoomLocked(false);

            desktopConnection.getClient().sendEvent(ROOM_UNLOCKED_EVENT);
        });
    }
}
