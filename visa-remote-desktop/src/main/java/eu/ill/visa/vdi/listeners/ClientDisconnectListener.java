package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSession;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.events.UserDisconnectedEvent;
import eu.ill.visa.vdi.events.UsersConnectedEvent;
import eu.ill.visa.vdi.models.DesktopConnection;
import eu.ill.visa.vdi.services.DesktopAccessService;
import eu.ill.visa.vdi.services.DesktopConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDisconnectListener extends AbstractListener implements DisconnectListener {

    private static final Logger logger = LoggerFactory.getLogger(ClientDisconnectListener.class);

    private final DesktopAccessService desktopAccessService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceService instanceService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;

    public ClientDisconnectListener(final DesktopConnectionService desktopConnectionService,
                                    final DesktopAccessService desktopAccessService,
                                    final InstanceSessionService instanceSessionService,
                                    final InstanceService instanceService,
                                    final VirtualDesktopConfiguration virtualDesktopConfiguration) {
        super(desktopConnectionService);
        this.desktopAccessService = desktopAccessService;
        this.instanceSessionService = instanceSessionService;
        this.instanceService = instanceService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
    }

    @Override
    public void onDisconnect(final SocketIOClient client) {
        final DesktopConnection connection = this.getDesktopConnection(client);

        if (connection != null) {
            connection.getConnectionThread().closeTunnel();

            client.leaveRoom(connection.getRoomId());

            final Instance instance = instanceService.getById(connection.getInstanceId());
            InstanceSession session = null;
            if (instance != null) {
                session = instanceSessionService.getByInstance(instance);
            }
            if (session != null) {
                // Remove client/member from instance session
                instanceSessionService.removeInstanceSessionMember(session, client.getSessionId());

                if (!this.desktopConnectionService.isOwnerConnected(instance) && !connection.getConnectedUser().getRole().equals(Role.SUPPORT)) {
                    if (this.virtualDesktopConfiguration.getOwnerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)) {
                        this.desktopConnectionService.lockRoom(client, connection.getRoomId(), instance);

                    } else {
                        logger.info("There is no owner ar admin connected so all clients will be disconnected");
                        this.desktopConnectionService.disconnectAllRoomClients(client, connection.getRoomId());
                    }

                } else {
                    // broadcast events for a user disconnected and current users
                    this.broadcast(client,
                        new UserDisconnectedEvent(this.getConnectedUser(client)),
                        new UsersConnectedEvent(instance, this.getConnectedUsers(instance, false))
                    );
                }

            } else {
                logger.info("There is no active instance session so all clients will be disconnected");
                this.desktopConnectionService.disconnectAllRoomClients(client, connection.getRoomId());
            }

            this.removeDesktopConnection(client);

        } else {
            this.desktopAccessService.cancelAccess(client);
        }
    }
}

