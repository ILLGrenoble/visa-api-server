package eu.ill.visa.vdi.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.common.utils.domain.Timer;
import eu.ill.visa.common.vdi.broker.*;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.vdi.domain.exceptions.ApiCommunicationException;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.*;
import eu.ill.visa.vdi.gateway.events.AccessCancellationEvent;
import eu.ill.visa.vdi.gateway.events.AccessRequestEvent;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Startup
@ApplicationScoped
public class DesktopAccessService {
    private static final Logger logger = LoggerFactory.getLogger(DesktopAccessService.class);
    private static final int NOP_INTERVAL_TIME_SECONDS = 5;

    private final DesktopSessionService desktopSessionService;
    private final MessageBroker messageBroker;
    private final EventDispatcher eventDispatcher;

    private final List<DesktopCandidate> desktopCandidates = new ArrayList<>();

    private final Cancellable keepAliveInterval;

    @Inject
    public DesktopAccessService(final DesktopSessionService desktopSessionService,
                                final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance,
                                final EventDispatcher eventDispatcher) {
        this.desktopSessionService = desktopSessionService;
        this.messageBroker = messageBrokerInstance.get();
        this.eventDispatcher = eventDispatcher;

        this.messageBroker.subscribe(AccessRequestMessage.class)
            .next((message) -> this.onAccessRequested(message.connectionId(), message.user(), message.requesterClientId()));
        this.messageBroker.subscribe(AccessRequestCancellationMessage.class)
            .next((message) -> this.onAccessRequestCancelled(message.connectionId(), message.user(), message.requesterClientId()));
        this.messageBroker.subscribe(AccessRequestResponseMessage.class)
            .next((message) -> this.onAccessRequestResponse(message.connectionId(), message.requesterClientId(), message.role()));
        this.messageBroker.subscribe(SessionClosedMessage.class)
            .next((message) -> this.onOwnerDisconnected(message.connectionId()));
        this.messageBroker.subscribe(SessionLockedMessage.class)
            .next((message) -> this.onOwnerDisconnected(message.connectionId()));

        this.keepAliveInterval = Timer.setInterval(() -> {
            this.desktopCandidates.forEach(DesktopCandidate::keepAlive);
        }, NOP_INTERVAL_TIME_SECONDS, TimeUnit.SECONDS);

    }

    @Shutdown
    public void shutdown() {
        this.keepAliveInterval.cancel();
    }

    public void requestAccess(final SocketClient client, final String connectionId, final ConnectedUser connectedUser, final VdiInstance instance, final NopSender nopSender) {
        // Create pending desktop connection
        DesktopCandidate desktopCandidate = this.addCandidate(client, connectionId, connectedUser, instance, nopSender);

        this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.ACCESS_PENDING_EVENT);

        this.messageBroker.broadcast(new AccessRequestMessage(connectionId, connectedUser, client.clientId()));
    }

    public void cancelAccessRequest(final SocketClient client) {
        // Verify that access has been requested
        this.removeCandidate(client.clientId()).ifPresent(desktopCandidate -> {
            // A desktop request was in progress
            this.messageBroker.broadcast(new AccessRequestCancellationMessage(desktopCandidate.connectionId(), desktopCandidate.connectedUser(), client.clientId()));
        });
    }

    public void respondToAccessRequest(final DesktopSessionMember ownerSessionMember, final String connectionId, final String requesterClientId, final InstanceMemberRole role) {
        // Verify that we have a connection and that the user is the owner
        if (ownerSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER)) {
            // Forward reply to broker once we've verified that the response comes from the owner
            this.messageBroker.broadcast(new AccessRequestResponseMessage(connectionId, requesterClientId, role));
        }
    }

    private void onAccessRequested(final String connectionId, final ConnectedUser user, final String requesterClientId) {
        // See if we have the owner of the instance
        this.desktopSessionService.findDesktopSession(connectionId).ifPresent(desktopSession -> {
            List<DesktopSessionMember> ownerSessionMembers = desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER)).toList();
            if (!ownerSessionMembers.isEmpty()) {
                logger.info("Handling access request for instance {} from user {} with client id {}", desktopSession.getInstance().id(), user.fullName(), requesterClientId);

                // Send request to owners
                ownerSessionMembers.forEach(ownerSessionMember -> {
                    this.eventDispatcher.sendEventToClient(ownerSessionMember.clientId(), SessionEvent.ACCESS_REQUEST_EVENT, new AccessRequestEvent(connectionId, user, requesterClientId));
                });
            }
        });
    }

    private void onAccessRequestCancelled(final String connectionId, final ConnectedUser user, final String requesterClientId) {
        this.desktopSessionService.findDesktopSession(connectionId).ifPresent(desktopSession -> {
            List<DesktopSessionMember> ownerSessionMembers = desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER)).toList();
            if (!ownerSessionMembers.isEmpty()) {
                logger.info("Handling cancellation for instance {} from user {} with client id {}", desktopSession.getInstance().id(), user.fullName(), requesterClientId);

                // Send cancellation to owners
                ownerSessionMembers.forEach(ownerSessionMember -> {
                    this.eventDispatcher.sendEventToClient(ownerSessionMember.clientId(), SessionEvent.ACCESS_CANCELLATION_EVENT, new AccessCancellationEvent(user.fullName(), requesterClientId));
                });
            }
        });
    }

    private void onAccessRequestResponse(final String connectionId, final String requesterClientId, final InstanceMemberRole role) {
        this.removeCandidate(requesterClientId).ifPresent(desktopCandidate -> {
            logger.info("Handling response ({}) of access request for instance {} from user {} with client id {}", role, desktopCandidate.instance().id(), desktopCandidate.connectedUser().fullName(), requesterClientId);
            this.connectFromAccessReply(desktopCandidate, role);
        });

        // Send message to any other owners that request has been handled
        this.desktopSessionService.findDesktopSession(connectionId).ifPresent(desktopSession -> {
            desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER)).forEach(ownerSessionMember -> {
                this.eventDispatcher.sendEventToClient(ownerSessionMember.clientId(), SessionEvent.ACCESS_REPLY_EVENT, new AccessRequestResponseEvent(connectionId, requesterClientId, role.toString()));
            });
        });
    }

    private void onOwnerDisconnected(final String connectionId) {
        this.getCandidatesBySessionId(connectionId).forEach(desktopCandidate -> {
            final SocketClient client = desktopCandidate.client();

            this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.OWNER_AWAY_EVENT);
            client.disconnect();
        });
    }

    private synchronized DesktopCandidate addCandidate(final SocketClient client, final String connectionId, final ConnectedUser connectedUser, final VdiInstance instance, final NopSender nopSender) {
        DesktopCandidate desktopCandidate = new DesktopCandidate(client, connectionId, connectedUser, instance, nopSender);
        this.desktopCandidates.add(desktopCandidate);

        return desktopCandidate;
    }

    private synchronized Optional<DesktopCandidate> getCandidateByClientId(final String clientId) {
        return this.desktopCandidates.stream()
            .filter(candidate -> candidate.client().clientId().equals(clientId))
            .findAny();
    }

    private synchronized Optional<DesktopCandidate> removeCandidate(final String clientId) {
        return this.getCandidateByClientId(clientId).stream()
            .peek(this.desktopCandidates::remove)
            .findAny();
    }

    private synchronized List<DesktopCandidate> getCandidatesBySessionId(final String connectionId) {
        return this.desktopCandidates.stream()
            .filter(candidate -> candidate.connectionId().equals(connectionId))
            .toList();
    }

    private void connectFromAccessReply(final DesktopCandidate candidate, final InstanceMemberRole replyRole) {

        final SocketClient client = candidate.client();
        if (client.isChannelOpen()) {
            ConnectedUser user = candidate.connectedUser();
            final VdiInstance instance = candidate.instance();

            // Convert the support role to a normal user one if the owner of the instance is staff
            InstanceMemberRole role = this.convertAccessReplyRole(replyRole, instance);
            ConnectedUser connectedUser = new ConnectedUser(user.id(), user.fullName(), role);
            try {
                this.desktopSessionService.createDesktopSessionMember(client, connectedUser, instance, candidate.connectionId(), candidate.nopSender());

                this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.ACCESS_GRANTED_EVENT, role);

            } catch (OwnerNotConnectedException exception) {
                this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.OWNER_AWAY_EVENT);
                client.disconnect();

            } catch (UnauthorizedException exception) {
                logger.warn(exception.getMessage());
                this.eventDispatcher.sendEventToClient(client.clientId(), SessionEvent.ACCESS_DENIED);
                client.disconnect();

            } catch (ConnectionException | ApiCommunicationException exception) {
                logger.error(exception.getMessage());
                client.disconnect();
            }

        } else {
            logger.info("Client {} is no longer waiting for an access reply", client.clientId());
        }
    }

    private InstanceMemberRole convertAccessReplyRole(InstanceMemberRole replyRole, VdiInstance instance) {
        if (replyRole.equals(InstanceMemberRole.SUPPORT)) {
            boolean ownerIsExternalUser = instance.ownerIsExternalUser();
            if (ownerIsExternalUser) {
                // See if user has right to access instance when owner away (support role, otherwise user role)
                if (instance.canConnectWhileOwnerAway()) {
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
