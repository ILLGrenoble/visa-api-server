package eu.ill.visa.vdi.business.services;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.models.Role;

public abstract class DesktopService {

    public abstract ConnectionThread connect(final SocketIOClient client,
                                             final Instance instance,
                                             final User user,
                                             final Role role) throws OwnerNotConnectedException, ConnectionException;


    protected String getInstanceAndUser(Instance instance, User user, Role role) {
        return "User " + user.getFullName() + " (" + user.getId() + ", " + role.toString() + "), Instance " + instance.getId();
    }
}
