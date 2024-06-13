package eu.ill.visa.vdi.services;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.vdi.domain.AccessCancellation;
import eu.ill.visa.vdi.domain.AccessReply;
import eu.ill.visa.vdi.domain.AccessRequest;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.events.AccessCancellationEvent;
import eu.ill.visa.vdi.events.AccessCandidateEvent;
import eu.ill.visa.vdi.events.AccessReplyEvent;
import eu.ill.visa.vdi.events.Event;
import eu.ill.visa.vdi.exceptions.ConnectionException;
import eu.ill.visa.vdi.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.models.DesktopCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static eu.ill.visa.vdi.events.Event.ACCESS_DENIED;

@ApplicationScoped
public class DesktopAccessService {
    private static final Logger logger = LoggerFactory.getLogger(DesktopAccessService.class);

    private final DesktopConnectionService desktopConnectionService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceService instanceService;

    private final Map<UUID, DesktopCandidate> desktopCandidates = new HashMap<>();
    private final Map<UUID, UUID> clientSessionIds = new HashMap<>();

    @Inject
    public DesktopAccessService(final DesktopConnectionService desktopConnectionService,
                                final InstanceSessionService instanceSessionService,
                                final InstanceService instanceService) {
        this.desktopConnectionService = desktopConnectionService;
        this.instanceSessionService = instanceSessionService;
        this.instanceService = instanceService;
    }

    private DesktopCandidate addCandidate(SocketIOClient client, User user, Instance instance) {
        DesktopCandidate desktopCandidate = new DesktopCandidate(client, user, instance.getId());
        this.desktopCandidates.put(client.getSessionId(), desktopCandidate);

        return desktopCandidate;
    }

    public void initiateAccess(SocketIOClient client, User user, Instance instance) {
        // Create pending desktop connection
        DesktopCandidate desktopCandidate = this.addCandidate(client, user, instance);

        // Get room Id from connection
        String room = desktopCandidate.getRoomId();

        client.joinRoom(room);

        final SocketIONamespace namespace = client.getNamespace();
        final BroadcastOperations operations = namespace.getRoomOperations(room);

        client.sendEvent(Event.ACCESS_PENDING_EVENT);

        SocketIOClient owner = this.getDesktopOwner(operations.getClients());
        if (owner == null) {
            // If owner not found on this server then broadcast to all other servers
            this.desktopConnectionService.broadcast(client, room, new AccessCandidateEvent(user.getFullName()));

        } else {
            this.sendRequestToOwner(user.getFullName(), client.getSessionId(), owner);
        }
    }

    public void cancelAccess(SocketIOClient client) {
        // Determine if access has been requested
        DesktopCandidate desktopCandidate = this.desktopCandidates.remove(client.getSessionId());
        if (desktopCandidate != null) {
            User user = desktopCandidate.getUser();

            // Get room Id from connection
            String room = desktopCandidate.getRoomId();

            // See if request was made to owner on local server
            UUID requestToken = this.getRequestTokenForClient(client.getSessionId());
            if (requestToken != null) {

                // Get owner
                final SocketIONamespace namespace = client.getNamespace();
                final BroadcastOperations operations = namespace.getRoomOperations(room);
                SocketIOClient owner = this.getDesktopOwner(operations.getClients());

                // Send cancellation to owner
                this.sendCancellationToOwner(user.getFullName(), requestToken, owner);

            } else {
                // Broadcast cancellation to other servers
                this.desktopConnectionService.broadcast(client, room, new AccessCancellationEvent(user.getFullName()));
            }
        }
    }

    public void forwardCandidateRequest(Collection<SocketIOClient> clients, String userFullName, UUID candidateSessionId) {
        SocketIOClient owner = this.getDesktopOwner(clients);
        if (owner != null) {
            this.sendRequestToOwner(userFullName, candidateSessionId, owner);
        }
    }

    public void forwardAccessCancellation(Collection<SocketIOClient> clients, String userFullName, UUID candidateSessionId) {
        SocketIOClient owner = this.getDesktopOwner(clients);
        if (owner != null) {
            UUID requestToken = this.getRequestTokenForClient(candidateSessionId);
            if (requestToken != null) {
                this.sendCancellationToOwner(userFullName, requestToken, owner);
            }
        }
    }

    private SocketIOClient getDesktopOwner(Collection<SocketIOClient> clients) {

        for (final SocketIOClient aClient : clients) {
            InstanceSessionMember instanceSessionMember = this.instanceSessionService.getSessionMemberBySessionId(aClient.getSessionId());
            if (instanceSessionMember != null && instanceSessionMember.getRole().equals("OWNER")) {
                return aClient;
            }
        }

        return null;
    }

    private UUID getRequestTokenForClient(UUID clientSessionId) {
        for (Map.Entry<UUID, UUID> entry : this.clientSessionIds.entrySet()) {
            if (entry.getValue().equals(clientSessionId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void sendRequestToOwner(String userFullName, UUID clientSessionId, SocketIOClient owner) {
        // Generate a token
        UUID requestToken = UUID.randomUUID();

        // Maintain a map of token to candidateSessionId
        this.clientSessionIds.put(requestToken, clientSessionId);

        // Send request to owner
        owner.sendEvent(Event.ACCESS_REQUEST_EVENT, new AccessRequest(userFullName, requestToken.toString()));
    }

    private void sendCancellationToOwner(String userFullName, UUID requestToken, SocketIOClient owner) {
        // Remove clientSessionId from map
        this.clientSessionIds.remove(requestToken);

        // Send cancellation to owner
        owner.sendEvent(Event.ACCESS_CANCELLATION_EVENT, new AccessCancellation(userFullName, requestToken.toString()));
    }

    public void onAccessReply(SocketIOClient owner, AccessReply accessReply) {
        UUID requestToken = UUID.fromString(accessReply.getId());
        UUID clientSessionId = this.clientSessionIds.remove(requestToken);
        Role replyRole = accessReply.getRole();

        DesktopCandidate candidate = this.desktopCandidates.remove(clientSessionId);
        if (candidate != null) {
            this.connectFromAccessReply(candidate, replyRole);

        } else {
            // Broadcast candidate response
            this.desktopConnectionService.broadcast(owner, new AccessReplyEvent(new AccessReply(clientSessionId.toString(), replyRole.toString())));
        }
    }

    public void handleForwardedAccessReply(AccessReply accessReply) {
        UUID clientSessionId = UUID.fromString(accessReply.getId());
        DesktopCandidate candidate = this.desktopCandidates.remove(clientSessionId);
        if (candidate != null) {
            Role replyRole = accessReply.getRole();
            this.connectFromAccessReply(candidate, replyRole);
        }
    }

    private Role convertAccessReplyRole(Role replyRole, Instance instance, User user) {
        if (replyRole.equals(Role.SUPPORT)) {
            InstanceMember owner = instance.getOwner();
            boolean ownerIsExternalUser = !owner.getUser().hasRole(eu.ill.visa.core.entity.Role.STAFF_ROLE);
            if (ownerIsExternalUser) {
                // See if user has right to access instance when owner away (support role, otherwise user role)
                if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user)) {
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

            User user = candidate.getUser();
            Instance instance = this.instanceService.getFullById(candidate.getInstanceId());

            if (instance != null) {
                // Convert the support role to a normal user one if the owner of the instance is staff
                Role role = this.convertAccessReplyRole(replyRole, instance, user);
                try {
                    this.desktopConnectionService.createDesktopConnection(client, instance, user, role);

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
