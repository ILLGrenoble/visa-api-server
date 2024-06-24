package eu.ill.visa.vdi.business.services;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.messages.AccessRequestCancellationMessage;
import eu.ill.visa.vdi.brokers.messages.AccessRequestMessage;
import eu.ill.visa.vdi.brokers.messages.AccessRequestResponseMessage;
import eu.ill.visa.vdi.domain.models.Event;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.DesktopCandidate;
import eu.ill.visa.vdi.domain.models.DesktopConnection;
import eu.ill.visa.vdi.gateway.events.AccessCancellationEvent;
import eu.ill.visa.vdi.gateway.events.AccessRequestEvent;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static eu.ill.visa.vdi.domain.models.Event.ACCESS_DENIED;

@Startup
@ApplicationScoped
public class DesktopAccessService {
    private static final Logger logger = LoggerFactory.getLogger(DesktopAccessService.class);

    private final DesktopConnectionService desktopConnectionService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceService instanceService;

    private final RemoteDesktopBroker remoteDesktopBroker;

    private final List<DesktopCandidate> desktopCandidates = new ArrayList<>();

    @Inject
    public DesktopAccessService(final DesktopConnectionService desktopConnectionService,
                                final InstanceSessionService instanceSessionService,
                                final InstanceService instanceService,
                                final jakarta.enterprise.inject.Instance<RemoteDesktopBroker> remoteDesktopBrokerInstance) {
        this.desktopConnectionService = desktopConnectionService;
        this.instanceSessionService = instanceSessionService;
        this.instanceService = instanceService;
        this.remoteDesktopBroker = remoteDesktopBrokerInstance.get();

        this.remoteDesktopBroker.subscribe(AccessRequestMessage.class)
            .next((message) -> this.onAccessRequested(message.instanceId(), message.user(), message.requesterConnectionId()));
        this.remoteDesktopBroker.subscribe(AccessRequestCancellationMessage.class)
            .next((message) -> this.onAccessRequestCancelled(message.instanceId(), message.user(), message.requesterConnectionId()));
        this.remoteDesktopBroker.subscribe(AccessRequestResponseMessage.class)
            .next((message) -> this.onAccessRequestResponse(message.instanceId(), message.requesterConnectionId(), message.role()));
    }

    public void requestAccess(SocketIOClient client, ConnectedUser user, Long instanceId) {
        // Create pending desktop connection
        DesktopCandidate desktopCandidate = this.addCandidate(client, user, instanceId);

        client.sendEvent(Event.ACCESS_PENDING_EVENT);

        this.remoteDesktopBroker.broadcast(new AccessRequestMessage(instanceId, user, desktopCandidate.getConnectionId()));
    }

    public void cancelAccessRequest(SocketIOClient client) {
        // Verify that access has been requested
        DesktopCandidate desktopCandidate = this.removeCandidate(client.getSessionId().toString());
        if (desktopCandidate != null) {
            // A desktop request was in progress
            this.remoteDesktopBroker.broadcast(new AccessRequestCancellationMessage(desktopCandidate.getInstanceId(), desktopCandidate.getUser(), desktopCandidate.getConnectionId()));
        }
    }

    public void respondToAccessRequest(Long instanceId, String requesterConnectionId, InstanceMemberRole role) {
        // Forward reply to broker
        this.remoteDesktopBroker.broadcast(new AccessRequestResponseMessage(instanceId, requesterConnectionId, role));
    }

    public void onAccessRequested(Long instanceId, ConnectedUser user, String requesterConnectionId) {
        // See if we have the owner of the instance
        List<DesktopConnection> ownerDesktopConnections = this.desktopConnectionService.getOwnerDesktopConnectionsForInstanceId(instanceId);
        if (!ownerDesktopConnections.isEmpty()) {
            logger.info("Handling access request for instance {} from user {} with client id {}", instanceId, user.getFullName(), requesterConnectionId);

            // Send request to owners
            ownerDesktopConnections.forEach(desktopConnection -> {
                desktopConnection.getClient().sendEvent(Event.ACCESS_REQUEST_EVENT, new AccessRequestEvent(instanceId, user, requesterConnectionId));
            });
        }
    }

    public void onAccessRequestCancelled(Long instanceId, ConnectedUser user, String requesterConnectionId) {
        List<DesktopConnection> ownerDesktopConnections = this.desktopConnectionService.getOwnerDesktopConnectionsForInstanceId(instanceId);
        if (!ownerDesktopConnections.isEmpty()) {
            logger.info("Handling cancellation for instance {} from user {} with client id {}", instanceId, user.getFullName(), requesterConnectionId);

            // Send cancellation to owners
            ownerDesktopConnections.forEach(desktopConnection -> {
                desktopConnection.getClient().sendEvent(Event.ACCESS_CANCELLATION_EVENT, new AccessCancellationEvent(user.getFullName(), requesterConnectionId));
            });
        }
    }

    public void onAccessRequestResponse(Long instanceId, String requesterConnectionId, InstanceMemberRole role) {
        DesktopCandidate candidate = this.removeCandidate(requesterConnectionId);
        if (candidate != null) {
            logger.info("Handling response ({}) of access request for instance {} from user {} with client id {}", role, candidate.getInstanceId(), candidate.getUser().getFullName(), requesterConnectionId);
            this.connectFromAccessReply(candidate, role);
        }

        // Send message to any other owners that request has been handled
        this.desktopConnectionService.getOwnerDesktopConnectionsForInstanceId(instanceId).forEach(desktopConnection -> {
            desktopConnection.getClient().sendEvent(Event.ACCESS_REPLY_EVENT, new AccessRequestResponseEvent(instanceId, requesterConnectionId, role.toString()));
        });
    }

    private synchronized DesktopCandidate addCandidate(SocketIOClient client, ConnectedUser user, Long instanceId) {
        DesktopCandidate desktopCandidate = new DesktopCandidate(client, user, instanceId);
        this.desktopCandidates.add(desktopCandidate);

        return desktopCandidate;
    }

    private synchronized DesktopCandidate getCandidateById(String connectionId) {
        return this.desktopCandidates.stream()
            .filter(candidate -> candidate.getConnectionId().equals(connectionId))
            .findAny()
            .orElse(null);
    }

    private synchronized DesktopCandidate removeCandidate(String connectionId) {
        DesktopCandidate desktopCandidate = this.getCandidateById(connectionId);
        if (desktopCandidate != null) {
            this.desktopCandidates.remove(desktopCandidate);
            return  desktopCandidate;
        }
        return null;
    }

    private void connectFromAccessReply(DesktopCandidate candidate, InstanceMemberRole replyRole) {

        SocketIOClient client = candidate.getClient();
        if (client.isChannelOpen()) {

            ConnectedUser user = candidate.getUser();
            Instance instance = this.instanceService.getFullById(candidate.getInstanceId());

            if (instance != null) {
                // Convert the support role to a normal user one if the owner of the instance is staff
                InstanceMemberRole role = this.convertAccessReplyRole(replyRole, instance, user);
                user.setRole(role);
                try {
                    this.desktopConnectionService.createDesktopConnection(client, instance, user);

                    client.sendEvent(Event.ACCESS_GRANTED_EVENT, role);

                } catch (OwnerNotConnectedException exception) {
                    client.sendEvent(Event.OWNER_AWAY_EVENT);
                    client.disconnect();

                } catch (UnauthorizedException exception) {
                    logger.warn(exception.getMessage());
                    client.sendEvent(ACCESS_DENIED);
                    client.disconnect();

                } catch (ConnectionException exception) {
                    logger.error(exception.getMessage());
                    client.disconnect();
                }
            } else {
                logger.error("Attempt to connect to instance {} by user {} that no longer exists", candidate.getInstanceId(), user.getFullName());
                client.disconnect();
            }

        } else {
            logger.info("Client {} is no longer waiting for an access reply", candidate.getClient().getSessionId());
        }
    }

    private InstanceMemberRole convertAccessReplyRole(InstanceMemberRole replyRole, Instance instance, ConnectedUser user) {
        if (replyRole.equals(InstanceMemberRole.SUPPORT)) {
            InstanceMember owner = instance.getOwner();
            boolean ownerIsExternalUser = !owner.getUser().hasRole(eu.ill.visa.core.entity.Role.STAFF_ROLE);
            if (ownerIsExternalUser) {
                // See if user has right to access instance when owner away (support role, otherwise user role)
                if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
                    // SUPPORT role if user can connect while owner away
                    return InstanceMemberRole.SUPPORT;

                } else {
                    // Standard USER role if user cannot connect if owner is away
                    return InstanceMemberRole.USER;
                }

            } else {
                // Standard user for staff
                return InstanceMemberRole.USER;
            }
        } else {
            return replyRole;
        }

    }
}
