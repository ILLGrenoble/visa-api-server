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
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.domain.events.*;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.DesktopConnection;
import eu.ill.visa.vdi.domain.models.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static eu.ill.visa.vdi.domain.events.Event.ACCESS_REVOKED_EVENT;
import static eu.ill.visa.vdi.domain.events.Event.OWNER_AWAY_EVENT;

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

    @Inject
    public DesktopConnectionService(final InstanceSessionService instanceSessionService,
                                    final UserService userService,
                                    final GuacamoleDesktopService guacamoleDesktopService,
                                    final WebXDesktopService webXDesktopService,
                                    final InstanceExpirationService instanceExpirationService,
                                    final VirtualDesktopConfiguration virtualDesktopConfiguration) {
        this.instanceSessionService = instanceSessionService;
        this.userService = userService;
        this.guacamoleDesktopService = guacamoleDesktopService;
        this.webXDesktopService = webXDesktopService;
        this.instanceExpirationService = instanceExpirationService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
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
            logger.info("User {} creating WebX desktop connection to instance {}", (user.getFullName() + "(" + role.toString() + ")"), instance.getId());
            thread = webXDesktopService.connect(client, instance, user);
        } else {
            logger.info("User {} creating Guacamole desktop connection to instance {}", (user.getFullName() + "(" + role.toString() + ")"), instance.getId());
            thread = guacamoleDesktopService.connect(client, instance, user);
        }

        DesktopConnection desktopConnection = new DesktopConnection(client, instance.getId(), instance.getLastSeenAt(), user, thread, instance.getId().toString());
        this.desktopConnections.add(desktopConnection);

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
            this.unlockRoom(client, desktopConnection.getRoomId(), instance);

        } else {
            this.broadcast(client,
                new UserConnectedEvent(this.getConnectedUser(client)),
                new UsersConnectedEvent(instance, this.getConnectedUsers(instance, false))
            );
        }

        return desktopConnection;
    }

    public synchronized DesktopConnection getDesktopConnection(final SocketIOClient client) {
        String clientId = client.getSessionId().toString();
        return this.desktopConnections.stream().filter(desktopConnection -> desktopConnection.getConnectionId().equals(clientId)).findAny().orElse(null);
    }

    public synchronized List<DesktopConnection> getDesktopConnectionsForInstanceId(Long instanceId) {
        return this.desktopConnections.stream().filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId)).toList();
    }

    public synchronized List<DesktopConnection> getOwnerDesktopConnectionsForInstanceId(Long instanceId) {
        return this.desktopConnections.stream()
            .filter(desktopConnection -> desktopConnection.getInstanceId().equals(instanceId))
            .filter(desktopConnection -> desktopConnection.getConnectedUser().getRole().equals(Role.OWNER))
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
        List<ConnectedUser> connectedUsers = instanceSessionMembers.stream().map(instanceSessionMember -> {
            User user = instanceSessionMember.getUser();
            Role role = Role.valueOf(instanceSessionMember.getRole());
            if (isRoomLocked && role.equals(Role.USER)) {
                role= Role.GUEST;
            }
            return new ConnectedUser(user.getId(), user.getFullName(), role);
        }).toList();

        return connectedUsers;
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

    public void disconnectAllRoomClients(final SocketIOClient client, final String room) {
        final SocketIONamespace namespace = client.getNamespace();

        final BroadcastOperations operations = namespace.getRoomOperations(room);
        final Collection<SocketIOClient> clients = operations.getClients();
        this.broadcast(client, new RoomClosedEvent());

        for (final SocketIOClient aClient : clients) {
            aClient.sendEvent(OWNER_AWAY_EVENT);
            aClient.disconnect();
        }
    }

    public void lockRoom(final SocketIOClient client, final String room, Instance instance) {
        final SocketIONamespace namespace = client.getNamespace();

        final BroadcastOperations operations = namespace.getRoomOperations(room);
        final Collection<SocketIOClient> clients = operations.getClients();

        // broadcast room closed and current connected users
        this.broadcast(client,
            new RoomLockedEvent(instance),
            new UsersConnectedEvent(instance, this.getConnectedUsers(instance, true))
        );

        for (final SocketIOClient aClient : clients) {
            DesktopConnection connection = this.getDesktopConnection(aClient);
            connection.setRoomLocked(true);
        }
    }

    public void unlockRoom(final SocketIOClient client, final String room, Instance instance) {
        final SocketIONamespace namespace = client.getNamespace();

        final BroadcastOperations operations = namespace.getRoomOperations(room);
        final Collection<SocketIOClient> clients = operations.getClients();

        // broadcast room closed and current connected users
        this.broadcast(client,
            new RoomUnlockedEvent(instance),
            new UsersConnectedEvent(instance, this.getConnectedUsers(instance, false))
        );

        for (final SocketIOClient aClient : clients) {
            DesktopConnection connection = this.getDesktopConnection(aClient);
            connection.setRoomLocked(false);
        }
    }

    public boolean disconnectClient(SocketIOClient owner, String room, UUID clientSessionId) {
        final SocketIONamespace namespace = owner.getNamespace();

        final BroadcastOperations operations = namespace.getRoomOperations(room);
        final Collection<SocketIOClient> clients = operations.getClients();

        Optional<SocketIOClient> clientOptional = clients.stream()
            .filter(aClient -> aClient.getSessionId().equals(clientSessionId))
            .findFirst();

        if (clientOptional.isPresent()) {
            logger.info("Disconnecting client {} from room {}", clientSessionId, room);
            SocketIOClient aClient = clientOptional.get();
            aClient.sendEvent(ACCESS_REVOKED_EVENT);
            aClient.disconnect();

            return true;

        } else {
            return false;
        }
    }
}
