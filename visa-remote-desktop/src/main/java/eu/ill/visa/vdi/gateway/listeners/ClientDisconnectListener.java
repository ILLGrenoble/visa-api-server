package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDisconnectListener implements DisconnectListener {

    private static final Logger logger = LoggerFactory.getLogger(ClientDisconnectListener.class);

    private final DesktopConnectionService desktopConnectionService;
    private final DesktopAccessService desktopAccessService;

    public ClientDisconnectListener(final DesktopConnectionService desktopConnectionService,
                                    final DesktopAccessService desktopAccessService) {
        this.desktopConnectionService = desktopConnectionService;
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onDisconnect(final SocketIOClient client) {
        final SocketClient socketClient = new SocketClient(client, client.getSessionId().toString());
        this.desktopConnectionService.findDesktopSessionMember(socketClient).ifPresentOrElse(this.desktopConnectionService::onDesktopMemberDisconnect, () -> {
            this.desktopAccessService.cancelAccessRequest(socketClient);
        });
    }
}

