package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;

public class ClientAccessRevokedCommandListener extends AbstractListener implements DataListener<AccessRevokedEvent> {

    public ClientAccessRevokedCommandListener(final DesktopConnectionService desktopConnectionService) {
        super(desktopConnectionService);
    }

    @Override
    public void onData(final SocketIOClient client, final AccessRevokedEvent command, final AckRequest ackRequest) {
        this.desktopConnectionService.revokeUserAccess(client, command.userId());
    }
}
