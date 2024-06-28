package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDisconnectListener implements DisconnectListener {

    private static final Logger logger = LoggerFactory.getLogger(ClientDisconnectListener.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;

    public ClientDisconnectListener(final DesktopSessionService desktopSessionService,
                                    final DesktopAccessService desktopAccessService) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onDisconnect(final SocketIOClient client) {
        final SocketClient socketClient = new SocketClient(client, client.getSessionId().toString());
        this.desktopSessionService.findDesktopSessionMember(socketClient).ifPresentOrElse(this.desktopSessionService::onDesktopMemberDisconnect, () -> {
            this.desktopAccessService.cancelAccessRequest(socketClient);
        });
    }
}

