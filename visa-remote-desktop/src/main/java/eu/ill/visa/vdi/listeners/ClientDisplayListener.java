package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSessionMember;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.models.DesktopConnection;
import eu.ill.visa.vdi.services.DesktopConnectionService;
import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static java.lang.String.format;

public class ClientDisplayListener extends AbstractListener implements DataListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(ClientDisplayListener.class);

    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;

    public ClientDisplayListener(final DesktopConnectionService desktopConnectionService,
                                 final InstanceService instanceService,
                                 final InstanceSessionService instanceSessionService) {
        super(desktopConnectionService);
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
    }

    @Override
    public void onData(final SocketIOClient client, final String data, final AckRequest ackRequest) {

        final DesktopConnection connection = this.getDesktopConnection(client);
        if (connection == null) {
            return;
        }

        final GuacamoleTunnel tunnel = connection.getConnectionThread().getTunnel();
        final GuacamoleWriter writer = tunnel.acquireWriter();

        try {
            int separatorPos = data.indexOf('.');
            int commandLength = Integer.parseInt(data.substring(0, separatorPos));
            String command = data.substring(separatorPos + 1, separatorPos + 1 + commandLength);
            boolean isControlAction = command.equals("mouse") || command.equals("key");
            if (isControlAction) {
                connection.updateLastInteractionAt();
            }

            Role role = connection.getConnectedUser().getRole();
            if (!isControlAction || role.equals(Role.OWNER) || role.equals(Role.SUPPORT) || (role.equals(Role.USER) && !connection.isRoomLocked())) {
                writer.write(data.toCharArray());
            }

            // Update last seen time of instance if more than one minute
            final Date lastSeenDate = connection.getLastSeenAt();
            final Date currentDate = new Date();
            if (lastSeenDate == null ||  (currentDate.getTime() - lastSeenDate.getTime() > 10 * 1000)) {
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
                    if(instanceSessionMember == null) {
                        logger.warn(format("Instance session member not found for instance %d", instanceId));
                    } else {
                        instanceSessionMember.updateLastSeenAt();
                        instanceSessionMember.setLastInteractionAt(connection.getLastInteractionAt());
                        instanceSessionService.saveInstanceSessionMember(instanceSessionMember);
                    }
                    connection.setLastSeenAt(instance.getLastSeenAt());
                }
            }

        } catch (GuacamoleConnectionClosedException exception) {
            logger.debug("Connection to guacd closed", exception);
        } catch (GuacamoleException exception) {
            logger.debug("WebSocket tunnel write failed", exception);
        } finally {
            tunnel.releaseWriter();
        }
    }
}
