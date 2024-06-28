package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;

public class ClientAccessRevokedCommandListener implements DataListener<AccessRevokedEvent> {

    private final DesktopSessionService desktopSessionService;

    public ClientAccessRevokedCommandListener(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    @Override
    public void onData(final SocketIOClient client, final AccessRevokedEvent command, final AckRequest ackRequest) {
        final SocketClient socketClient = new SocketClient(client, client.getSessionId().toString());
        this.desktopSessionService.findDesktopSessionMember(socketClient).ifPresent(desktopSessionMember -> {
            this.desktopSessionService.revokeUserAccess(desktopSessionMember, command.userId());
        });
    }
}
