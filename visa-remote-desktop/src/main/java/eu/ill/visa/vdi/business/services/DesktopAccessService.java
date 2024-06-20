package eu.ill.visa.vdi.business.services;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.domain.events.AccessReplyEvent;
import eu.ill.visa.vdi.domain.events.Event;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static eu.ill.visa.vdi.domain.events.Event.ACCESS_DENIED;

@ApplicationScoped
public class DesktopAccessService {
    private static final Logger logger = LoggerFactory.getLogger(DesktopAccessService.class);

    private record AccessRequestToken(String connectionId, ConnectedUser user, String token) { }

    private final DesktopConnectionService desktopConnectionService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceService instanceService;
    private final RemoteDesktopBroker remoteDesktopBroker;

    private final List<DesktopCandidate> desktopCandidates = new ArrayList<>();
    private final List<AccessRequestToken> accessRequestTokens = new ArrayList<>();

    @Inject
    public DesktopAccessService(final DesktopConnectionService desktopConnectionService,
                                final InstanceSessionService instanceSessionService,
                                final InstanceService instanceService,
                                final jakarta.enterprise.inject.Instance<RemoteDesktopBroker> remoteDesktopBroker) {
        this.desktopConnectionService = desktopConnectionService;
        this.instanceSessionService = instanceSessionService;
        this.instanceService = instanceService;
        this.remoteDesktopBroker = remoteDesktopBroker.get();
    }

    private DesktopCandidate addCandidate(SocketIOClient client, ConnectedUser user, Long instanceId) {
        DesktopCandidate desktopCandidate = new DesktopCandidate(client, user, instanceId);
        this.desktopCandidates.add(desktopCandidate);

        return desktopCandidate;
    }

    private DesktopCandidate getCandidateById(String connectionId) {
        return this.desktopCandidates.stream()
            .filter(candidate -> candidate.getConnectionId().equals(connectionId))
            .findAny()
            .orElse(null);
    }

    private DesktopCandidate removeCandidate(String connectionId) {
        DesktopCandidate desktopCandidate = this.getCandidateById(connectionId);
        if (desktopCandidate != null) {
            this.desktopCandidates.remove(desktopCandidate);
            return  desktopCandidate;
        }
        return null;
    }

    public void initiateAccess(SocketIOClient client, ConnectedUser user, Long instanceId) {
        // Create pending desktop connection
        DesktopCandidate desktopCandidate = this.addCandidate(client, user, instanceId);

        // Get room Id from connection
        String room = desktopCandidate.getRoomId();

        client.joinRoom(room);

        this.remoteDesktopBroker.onAccessRequested(instanceId, user, client.getSessionId().toString());
    }

    public void onAccessRequested(Long instanceId, ConnectedUser user, String requesterConnectionId) {
        // See if we have the owner of the instance
        List<DesktopConnection> ownerDesktopConnections = this.desktopConnectionService.getOwnerDesktopConnectionsForInstanceId(instanceId);
        if (!ownerDesktopConnections.isEmpty()) {
            logger.info("Handling access request for instance {} from user {} with client id {}", instanceId, user.getFullName(), requesterConnectionId);

            // Generate a token
            String requestToken = UUID.randomUUID().toString();

            // Maintain a map of token to candidateConnectionId
            this.accessRequestTokens.add(new AccessRequestToken(requesterConnectionId, user, requestToken));

            // Send request to owners
            ownerDesktopConnections.forEach(desktopConnection -> {
                SocketIOClient owner = desktopConnection.getClient();
                owner.sendEvent(Event.ACCESS_REQUEST_EVENT, new AccessRequest(user, requestToken));
            });
        } else {
            logger.info("Ignoring access request for instance {} from user {} with client id {}", instanceId, user.getFullName(), requesterConnectionId);
        }
    }

    public void cancelAccess(SocketIOClient client) {
        // Verify that access has been requested
        DesktopCandidate desktopCandidate = this.removeCandidate(client.getSessionId().toString());
        if (desktopCandidate != null) {
            // A desktop request was in progress
            this.remoteDesktopBroker.onAccessCancelled(desktopCandidate.getInstanceId(), desktopCandidate.getUser(), desktopCandidate.getConnectionId());
        }
    }

    public void onAccessRequestCancelled(Long instanceId, ConnectedUser user, String requesterConnectionId) {
        AccessRequestToken token = this.getRequestTokenForConnectionId(requesterConnectionId);
        if (token != null) {
            logger.info("Handling cancellation for instance {} from user {} with client id {}", instanceId, user.getFullName(), requesterConnectionId);
            List<DesktopConnection> ownerDesktopConnections = this.desktopConnectionService.getOwnerDesktopConnectionsForInstanceId(instanceId);
            // Send cancellation to owners
            ownerDesktopConnections.forEach(desktopConnection -> {
                SocketIOClient owner = desktopConnection.getClient();
                owner.sendEvent(Event.ACCESS_CANCELLATION_EVENT, new AccessCancellation(user.getFullName(), token.token()));
            });

            // Cleanup the map of tokens for candidateConnectionIds
            this.accessRequestTokens.remove(token);

        } else {
            logger.info("Ignoring cancellation for instance {} from user {} with client id {}", instanceId, user.getFullName(), requesterConnectionId);
        }
    }

    private AccessRequestToken getRequestTokenForConnectionId(String connectionId) {
        return this.accessRequestTokens.stream()
            .filter(token -> token.connectionId().equals(connectionId))
            .findAny()
            .orElse(null);
    }

    private AccessRequestToken getRequestTokenForToken(String token) {
        return this.accessRequestTokens.stream()
            .filter(accessRequestToken -> accessRequestToken.token().equals(token))
            .findAny()
            .orElse(null);
    }

    public void onAccessReply(SocketIOClient owner, AccessReply accessReply) {
        AccessRequestToken accessRequestToken = this.getRequestTokenForToken(accessReply.getId());
        if (accessRequestToken != null) {
            this.accessRequestTokens.remove(accessRequestToken);
            Role replyRole = accessReply.getRole();

            DesktopCandidate candidate = this.removeCandidate(accessRequestToken.connectionId());
            if (candidate != null) {
                this.connectFromAccessReply(candidate, replyRole);

            } else {
                // Broadcast candidate response
                this.desktopConnectionService.broadcast(owner, new AccessReplyEvent(new AccessReply(accessRequestToken.connectionId(), replyRole.toString())));
            }
        }
    }

    public void handleForwardedAccessReply(AccessReply accessReply) {
        DesktopCandidate candidate = this.removeCandidate(accessReply.getId());
        if (candidate != null) {
            Role replyRole = accessReply.getRole();
            this.connectFromAccessReply(candidate, replyRole);
        }
    }

    private Role convertAccessReplyRole(Role replyRole, Instance instance, ConnectedUser user) {
        if (replyRole.equals(Role.SUPPORT)) {
            InstanceMember owner = instance.getOwner();
            boolean ownerIsExternalUser = !owner.getUser().hasRole(eu.ill.visa.core.entity.Role.STAFF_ROLE);
            if (ownerIsExternalUser) {
                // See if user has right to access instance when owner away (support role, otherwise user role)
                if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
                    // SUPPORT role if user can connect while owner away
                    return Role.SUPPORT;

                } else {
                    // Standard USER role if user cannot connect if owner is away
                    return Role.USER;
                }

            } else {
                // Standard user for staff
                return Role.USER;
            }
        } else {
            return replyRole;
        }

    }

    private void connectFromAccessReply(DesktopCandidate candidate, Role replyRole) {

        SocketIOClient client = candidate.getClient();
        if (client.isChannelOpen()) {

            ConnectedUser user = candidate.getUser();
            Instance instance = this.instanceService.getFullById(candidate.getInstanceId());

            if (instance != null) {
                // Convert the support role to a normal user one if the owner of the instance is staff
                Role role = this.convertAccessReplyRole(replyRole, instance, user);
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
}
