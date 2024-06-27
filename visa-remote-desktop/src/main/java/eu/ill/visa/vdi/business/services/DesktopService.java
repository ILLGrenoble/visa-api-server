package eu.ill.visa.vdi.business.services;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;

public abstract class DesktopService {

    public final static String GUACAMOLE_PROTOCOL = "guacamole";
    public final static String WEBX_PROTOCOL = "webx";

    public abstract RemoteDesktopConnection connect(final SocketClient client,
                                                    final Instance instance,
                                                    final ConnectedUser user) throws OwnerNotConnectedException, ConnectionException;


    protected String getInstanceAndUser(Instance instance, ConnectedUser user) {
        return "User " + user.getFullName() + " (" + user.getId() + ", " + user.getRole().toString() + "), Instance " + instance.getId();
    }
}
