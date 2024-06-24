package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.DesktopConnection;

public class AbstractListener {

    protected final DesktopConnectionService desktopConnectionService;

    public AbstractListener(DesktopConnectionService desktopConnectionService) {
        this.desktopConnectionService = desktopConnectionService;
    }

    public DesktopConnection getDesktopConnection(final SocketIOClient client) {
        return this.desktopConnectionService.getDesktopConnection(client);
    }

    public void removeDesktopConnection(final SocketIOClient client) {
        this.desktopConnectionService.removeDesktopConnection(client);
    }

    public ConnectedUser getConnectedUser(final SocketIOClient client) {
        return this.desktopConnectionService.getConnectedUser(client);
    }
}
