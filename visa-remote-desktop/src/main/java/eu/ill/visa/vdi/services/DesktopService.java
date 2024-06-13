package eu.ill.visa.vdi.services;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.vdi.concurrency.ConnectionThread;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.exceptions.ConnectionException;
import eu.ill.visa.vdi.exceptions.OwnerNotConnectedException;

public abstract class DesktopService {

    private final CloudClientService cloudClientService;

    public DesktopService(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    public abstract ConnectionThread connect(final SocketIOClient client,
                                             final Instance instance,
                                             final User user,
                                             final Role role) throws OwnerNotConnectedException, ConnectionException;


    protected String getIpAddressForInstance(Instance instance) throws CloudException {
        if (instance.getIpAddress() == null) {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(instance.getCloudId());
            return cloudClient.ip(instance.getComputeId());
        }
        return instance.getIpAddress();
    }

    protected String getInstanceAndUser(Instance instance, User user, Role role) {
        return "User " + user.getFullName() + " (" + user.getId() + ", " + role.toString() + "), Instance " + instance.getId();
    }
}
