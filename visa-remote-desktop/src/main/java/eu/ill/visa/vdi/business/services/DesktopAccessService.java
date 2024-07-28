package eu.ill.visa.vdi.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.broker.AccessRequestCancellationMessage;
import eu.ill.visa.vdi.broker.AccessRequestMessage;
import eu.ill.visa.vdi.broker.AccessRequestResponseMessage;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.*;
import eu.ill.visa.vdi.gateway.events.AccessCancellationEvent;
import eu.ill.visa.vdi.gateway.events.AccessRequestEvent;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Startup
@ApplicationScoped
public class DesktopAccessService {
    private static final Logger logger = LoggerFactory.getLogger(DesktopAccessService.class);

    private final DesktopSessionService desktopSessionService;
    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;
    private final MessageBroker messageBroker;
    private final EventDispatcher eventDispatcher;

    private final List<DesktopCandidate> desktopCandidates = new ArrayList<>();

    private boolean keepingAlive = true;

    @Inject
    public DesktopAccessService(final DesktopSessionService desktopSessionService,
                                final InstanceService instanceService,
                                final InstanceSessionService instanceSessionService,
                                final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance,
                                final EventDispatcher eventDispatcher) {
        this.desktopSessionService = desktopSessionService;
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.messageBroker = messageBrokerInstance.get();
        this.eventDispatcher = eventDispatcher;

        this.messageBroker.subscribe(AccessRequestMessage.class)
            .next((message) -> this.onAccessRequested(message.sessionId(), message.user(), message.requesterClientId()));
        this.messageBroker.subscribe(AccessRequestCancellationMessage.class)
            .next((message) -> this.onAccessRequestCancelled(message.sessionId(), message.user(), message.requesterClientId()));
        this.messageBroker.subscribe(AccessRequestResponseMessage.class)
            .next((message) -> this.onAccessRequestResponse(message.sessionId(), message.requesterClientId(), message.role()));

        Thread keepAliveThread = new Thread(() -> {
            while (this.keepingAlive) {
                try {
                    Thread.sleep(1000);
                    this.desktopCandidates.forEach(DesktopCandidate::keepAlive);
                } catch (InterruptedException ignored) {
                }
            }
        });
        keepAliveThread.start();
    }

    @Shutdown
    public void shutdown() {
        this.keepingAlive = false;
    }

    public void requestAccess(final SocketClient client, final Long sessionId, final ConnectedUser connectedUser, Long instanceId, final NopSender nopSender) {
        // Create pending desktop connection
        DesktopCandidate desktopCandidate = this.addCandidate(client, sessionId, connectedUser, instanceId, nopSender);

        this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.ACCESS_PENDING_EVENT);

        this.messageBroker.broadcast(new AccessRequestMessage(sessionId, connectedUser, client.clientId()));
    }

    public void cancelAccessRequest(final SocketClient client) {
        // Verify that access has been requested
        this.removeCandidate(client.clientId()).ifPresent(desktopCandidate -> {
            // A desktop request was in progress
            this.messageBroker.broadcast(new AccessRequestCancellationMessage(desktopCandidate.sessionId(), desktopCandidate.connectedUser(), client.clientId()));
        });
    }

    public void respondToAccessRequest(final DesktopSessionMember ownerSessionMember, final Long sessionId, final String requesterConnectionId, final InstanceMemberRole role) {
        // Verify that we have a connection and that the user is the owner
        if (ownerSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER)) {
            // Forward reply to broker once we've verified that the response comes from the owner
            this.messageBroker.broadcast(new AccessRequestResponseMessage(sessionId, requesterConnectionId, role));
        }
    }

    private void onAccessRequested(final Long sessionId, final ConnectedUser user, final String requesterClientId) {
        // See if we have the owner of the instance
        this.desktopSessionService.findDesktopSession(sessionId).ifPresent(desktopSession -> {
            List<DesktopSessionMember> ownerSessionMembers = desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER)).toList();
            if (!ownerSessionMembers.isEmpty()) {
                logger.info("Handling access request for instance {} from user {} with client id {}", desktopSession.getInstanceId(), user.getFullName(), requesterClientId);

                // Send request to owners
                ownerSessionMembers.forEach(ownerSessionMember -> {
                    this.eventDispatcher.sendEventToClient(ownerSessionMember.clientId(), SessionEvent.ACCESS_REQUEST_EVENT, new AccessRequestEvent(sessionId, user, requesterClientId));
                });
            }
        });
    }

    private void onAccessRequestCancelled(final Long sessionId, final ConnectedUser user, final String requesterClientId) {
        this.desktopSessionService.findDesktopSession(sessionId).ifPresent(desktopSession -> {
            List<DesktopSessionMember> ownerSessionMembers = desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER)).toList();
            if (!ownerSessionMembers.isEmpty()) {
                logger.info("Handling cancellation for instance {} from user {} with client id {}", desktopSession.getInstanceId(), user.getFullName(), requesterClientId);

                // Send cancellation to owners
                ownerSessionMembers.forEach(ownerSessionMember -> {
                    this.eventDispatcher.sendEventToClient(ownerSessionMember.clientId(), SessionEvent.ACCESS_CANCELLATION_EVENT, new AccessCancellationEvent(user.getFullName(), requesterClientId));
                });
            }
        });
    }

    public void onAccessRequestResponse(final Long sessionId, final String requesterClientId, final InstanceMemberRole role) {
        this.removeCandidate(requesterClientId).ifPresent(desktopCandidate -> {
            logger.info("Handling response ({}) of access request for instance {} from user {} with client id {}", role, desktopCandidate.instanceId(), desktopCandidate.connectedUser().getFullName(), requesterClientId);
            this.connectFromAccessReply(desktopCandidate, role);
        });

        // Send message to any other owners that request has been handled
        this.desktopSessionService.findDesktopSession(sessionId).ifPresent(desktopSession -> {
            desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().getRole().equals(InstanceMemberRole.OWNER)).forEach(ownerSessionMember -> {
                this.eventDispatcher.sendEventToClient(ownerSessionMember.clientId(), SessionEvent.ACCESS_REPLY_EVENT, new AccessRequestResponseEvent(sessionId, requesterClientId, role.toString()));
            });
        });
    }

    private synchronized DesktopCandidate addCandidate(final SocketClient client, final Long sessionId, final ConnectedUser connectedUser, final Long instanceId, final NopSender nopSender) {
        DesktopCandidate desktopCandidate = new DesktopCandidate(client, sessionId, connectedUser, instanceId, nopSender);
        this.desktopCandidates.add(desktopCandidate);

        return desktopCandidate;
    }

    private synchronized Optional<DesktopCandidate> getCandidateByClientId(String clientId) {
        return this.desktopCandidates.stream()
            .filter(candidate -> candidate.client().clientId().equals(clientId))
            .findAny();
    }

    private synchronized Optional<DesktopCandidate> removeCandidate(String clientId) {
        return this.getCandidateByClientId(clientId).stream()
            .peek(this.desktopCandidates::remove)
            .findAny();
    }

    private void connectFromAccessReply(final DesktopCandidate candidate, final InstanceMemberRole replyRole) {

        final SocketClient client = candidate.client();
        if (client.isChannelOpen()) {
            final ConnectedUser user = candidate.connectedUser();
            final Long instanceId = candidate.instanceId();
            final Instance instance = this.instanceService.getFullById(instanceId);
            if (instance != null) {
                // Convert the support role to a normal user one if the owner of the instance is staff
                InstanceMemberRole role = this.convertAccessReplyRole(replyRole, instance, user);
                user.setRole(role);
                try {
                    this.desktopSessionService.createDesktopSessionMember(client, user, instance);

                    this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.ACCESS_GRANTED_EVENT, role);

                } catch (OwnerNotConnectedException exception) {
                    this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.OWNER_AWAY_EVENT);
                    client.disconnect();

                } catch (UnauthorizedException exception) {
                    logger.warn(exception.getMessage());
                    this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.ACCESS_DENIED);
                    client.disconnect();

                } catch (ConnectionException exception) {
                    logger.error(exception.getMessage());
                    client.disconnect();
                }

            } else {
                logger.info("Instance {} no longer exists for access reply", instanceId);
            }

        } else {
            logger.info("Client {} is no longer waiting for an access reply", client.clientId());
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
