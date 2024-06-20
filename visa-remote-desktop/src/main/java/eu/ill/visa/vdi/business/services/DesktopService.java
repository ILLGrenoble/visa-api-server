package eu.ill.visa.vdi.business.services;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;

public abstract class DesktopService {

    public abstract ConnectionThread connect(final SocketIOClient client,
                                             final Instance instance,
                                             final ConnectedUser user) throws OwnerNotConnectedException, ConnectionException;


    protected String getInstanceAndUser(Instance instance, ConnectedUser user) {
        return "User " + user.getFullName() + " (" + user.getId() + ", " + user.getRole().toString() + "), Instance " + instance.getId();
    }
}
