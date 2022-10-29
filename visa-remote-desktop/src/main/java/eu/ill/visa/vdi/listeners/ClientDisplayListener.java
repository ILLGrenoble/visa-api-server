package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSessionMember;
import eu.ill.visa.core.domain.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.concurrency.ConnectionThread;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.models.DesktopConnection;
import eu.ill.visa.vdi.services.DesktopConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static java.lang.String.format;

public abstract class ClientDisplayListener<T> extends AbstractListener implements DataListener<T> {

    private static final Logger logger = LoggerFactory.getLogger(ClientDisplayListener.class);

    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceActivityService instanceActivityService;

    public ClientDisplayListener(final DesktopConnectionService desktopConnectionService,
                                 final InstanceService instanceService,
                                 final InstanceSessionService instanceSessionService,
                                 final InstanceActivityService instanceActivityService) {
        super(desktopConnectionService);
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.instanceActivityService = instanceActivityService;
    }

    @Override
    public void onData(final SocketIOClient client, final T data, final AckRequest ackRequest) {

        final DesktopConnection connection = this.getDesktopConnection(client);
        if (connection == null) {
            return;
        }

        InstanceActivityType controlActivityType = this.getControlActivityType(data);
        if (controlActivityType != null) {
            connection.setInstanceActivity(controlActivityType);
        }

        Role role = connection.getConnectedUser().getRole();
        if (controlActivityType == null || role.equals(Role.OWNER) || role.equals(Role.SUPPORT) || (role.equals(Role.USER) && !connection.isRoomLocked())) {
            this.writeData(connection.getConnectionThread(), data);
        }

        // Update last seen time of instance if more than one minute
        final Date lastSeenDate = connection.getLastSeenAt();
        final Date currentDate = new Date();
        if (lastSeenDate == null || (currentDate.getTime() - lastSeenDate.getTime() > 5 * 1000)) {
            final Long instanceId = connection.getInstanceId();
            final Instance instance = instanceService.getById(instanceId);
            if (instance == null) {
                logger.warn(format("Instance not found %d for connected user %s with role %s", instanceId, connection.getConnectedUser().getFullName(), role));
                client.disconnect();
            } else {
                instance.updateLastSeenAt();

                if (lastSeenDate == null || lastSeenDate.getTime() <= connection.getLastInteractionAt().getTime()) {
                    instance.setLastInteractionAt(connection.getLastInteractionAt());
                }

                instanceService.save(instance);

                final InstanceSessionMember instanceSessionMember = this.instanceSessionService.getSessionMemberBySessionId(client.getSessionId());
                if (instanceSessionMember == null) {
                    logger.warn(format("Instance session member not found for instance %d", instanceId));
                } else {
                    instanceSessionMember.updateLastSeenAt();
                    instanceSessionMember.setLastInteractionAt(connection.getLastInteractionAt());
                    instanceSessionService.saveInstanceSessionMember(instanceSessionMember);

                    InstanceActivityType instanceActivityType = connection.getInstanceActivity();
                    if (instanceActivityType != null) {
                        this.instanceActivityService.create(instanceSessionMember.getUser(), instance, instanceActivityType);
                        connection.resetInstanceActivity();
                    }
                }
                connection.setLastSeenAt(instance.getLastSeenAt());
            }
        }
    }

    protected abstract InstanceActivityType getControlActivityType(T data);

    protected abstract void writeData(ConnectionThread connectionThread, T data);
}
