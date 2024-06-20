package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.vdi.domain.events.Event;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.DesktopConnection;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;

import java.util.List;

public class AbstractListener {

    protected final DesktopConnectionService desktopConnectionService;

    public AbstractListener(DesktopConnectionService desktopConnectionService) {
        this.desktopConnectionService = desktopConnectionService;
    }

    public void broadcast(final SocketIOClient client, final Event ...events) {
        this.desktopConnectionService.broadcast(client, events);
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

    public List<ConnectedUser> getConnectedUsers(Instance instance, boolean isRoomLocked) {
        return this.desktopConnectionService.getConnectedUsers(instance, isRoomLocked);
    }
}
