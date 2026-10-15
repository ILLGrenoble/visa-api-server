package eu.ill.visa.vdi.business.services;

import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;

public abstract class DesktopService {

    public final static String GUACAMOLE_PROTOCOL = "guacamole";
    public final static String WEBX_PROTOCOL = "webx";

    public abstract RemoteDesktopConnection connect(final SocketClient client,
                                                    final VdiInstance instance,
                                                    final ConnectedUser user,
                                                    final String connectionId) throws OwnerNotConnectedException, ConnectionException;


    protected String getInstanceAndUser(VdiInstance instance, ConnectedUser user) {
        return "User " + user.fullName() + " (" + user.id() + ", " + user.role().toString() + "), Instance " + instance.id();
    }
}
