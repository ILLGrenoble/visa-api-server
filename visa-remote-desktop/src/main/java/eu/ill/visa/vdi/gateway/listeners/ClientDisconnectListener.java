package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.domain.models.DesktopConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDisconnectListener  implements DisconnectListener {

    private static final Logger logger = LoggerFactory.getLogger(ClientDisconnectListener.class);

    private final DesktopConnectionService desktopConnectionService;
    private final DesktopAccessService desktopAccessService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceService instanceService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;

    public ClientDisconnectListener(final DesktopConnectionService desktopConnectionService,
                                    final DesktopAccessService desktopAccessService,
                                    final InstanceSessionService instanceSessionService,
                                    final InstanceService instanceService,
                                    final VirtualDesktopConfiguration virtualDesktopConfiguration) {
        this.desktopConnectionService = desktopConnectionService;
        this.desktopAccessService = desktopAccessService;
        this.instanceSessionService = instanceSessionService;
        this.instanceService = instanceService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
    }

    @Override
    public void onDisconnect(final SocketIOClient client) {
        final DesktopConnection connection = this.desktopConnectionService.getDesktopConnection(client);

        if (connection != null) {
            connection.getConnectionThread().closeTunnel();

            final Instance instance = instanceService.getById(connection.getInstanceId());
            if (instance != null) {
                InstanceSession session = instanceSessionService.getByInstance(instance);
                if (session != null) {
                    // Remove client/member from instance session
                    instanceSessionService.removeInstanceSessionMember(session, client.getSessionId());

                    if (!this.desktopConnectionService.isOwnerConnected(instance) && !connection.getConnectedUser().getRole().equals(InstanceMemberRole.SUPPORT)) {
                        if (this.virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)) {
                            this.desktopConnectionService.lockRoom(instance);

                        } else {
                            logger.info("There is no owner ar admin connected so all clients will be disconnected");
                            this.desktopConnectionService.closeRoom(instance);
                        }

                    } else {
                        // broadcast events for a user disconnected and current users
                        this.desktopConnectionService.disconnectUser(instance, this.desktopConnectionService.getConnectedUser(client), connection.getConnectionId());
                    }

                } else {
                    logger.info("There is no active instance session so all clients will be disconnected");
                    this.desktopConnectionService.closeRoom(instance);
                }
            }

            this.desktopConnectionService.removeDesktopConnection(client);

        } else {
            this.desktopAccessService.cancelAccessRequest(client);
        }
    }
}

