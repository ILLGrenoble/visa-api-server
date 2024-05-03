package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.vdi.domain.AccessRevokedCommand;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.events.AccessRevokedEvent;
import eu.ill.visa.vdi.models.DesktopConnection;
import eu.ill.visa.vdi.services.DesktopConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientAccessRevokedCommandListener extends AbstractListener implements DataListener<AccessRevokedCommand> {

    private static final Logger logger = LoggerFactory.getLogger(ClientAccessRevokedCommandListener.class);

    private final InstanceSessionService instanceSessionService;
    private final InstanceService instanceService;

    public ClientAccessRevokedCommandListener(final DesktopConnectionService desktopConnectionService,
                                    final InstanceSessionService instanceSessionService,
                                    final InstanceService instanceService) {
        super(desktopConnectionService);
        this.instanceSessionService = instanceSessionService;
        this.instanceService = instanceService;
    }

    @Override
    public void onData(final SocketIOClient client, final AccessRevokedCommand command, final AckRequest ackRequest) {
        final DesktopConnection connection = this.getDesktopConnection(client);

        if (connection != null) {
            if (connection.getConnectedUser().getRole().equals(Role.OWNER)) {
                final Instance instance = instanceService.getById(connection.getInstanceId());
                if (instance != null) {
                    logger.info("Owner has revoked access to remote desktop for instance {} for user with Id {}", connection.getInstanceId(), command.getUserId());

                    // Get all session members
                    List<InstanceSessionMember> members = instanceSessionService.getAllSessionMembers(instance);

                    // Find sessions associated to the userId in the revoke command
                    List<InstanceSessionMember> membersToRevoke = members.stream()
                        .filter(instanceSessionMember -> instanceSessionMember.getUser().getId().equals(command.getUserId()))
                        .collect(Collectors.toUnmodifiableList());

                    // Disconnect all associated sessions
                    membersToRevoke.forEach(instanceSessionMember -> {
                        UUID revokedSessionId = UUID.fromString(instanceSessionMember.getSessionId());
                        if (!this.desktopConnectionService.disconnectClient(client, connection.getRoomId(), revokedSessionId)) {
                            // Broadcast event to all servers if client not found locally
                            this.broadcast(client, new AccessRevokedEvent(connection.getRoomId(), revokedSessionId));
                        }
                    });
                }

            } else {
                logger.warn("A non-owner {} is trying to revoke the access to a remote desktop for instance {}", connection.getConnectedUser().getFullName(), connection.getInstanceId());
            }
        }
    }
}
