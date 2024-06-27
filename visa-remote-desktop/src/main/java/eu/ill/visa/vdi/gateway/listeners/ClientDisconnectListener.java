package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
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
        final SocketClient socketClient = new SocketClient(client, client.getSessionId().toString());
        this.desktopConnectionService.findDesktopSessionMember(socketClient).ifPresentOrElse(this.desktopConnectionService::onDesktopMemberDisconnect, () -> {
            this.desktopAccessService.cancelAccessRequest(socketClient);
        });
    }
}

