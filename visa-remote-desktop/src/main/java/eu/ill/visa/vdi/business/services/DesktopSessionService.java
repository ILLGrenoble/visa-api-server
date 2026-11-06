package eu.ill.visa.vdi.business.services;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.common.vdi.apiconnect.VisaApiService;
import eu.ill.visa.common.vdi.broker.*;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.common.utils.domain.SampleStats;
import eu.ill.visa.common.utils.domain.Timer;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.vdi.business.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.*;
import eu.ill.visa.vdi.gateway.events.UserConnectedEvent;
import eu.ill.visa.vdi.gateway.events.UserDisconnectedEvent;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static eu.ill.visa.vdi.domain.models.SessionEvent.*;

@Startup
@ApplicationScoped
public class DesktopSessionService {

    private final static Logger logger = LoggerFactory.getLogger(DesktopSessionService.class);

    private final GuacamoleDesktopService guacamoleDesktopService;
    private final WebXDesktopService webXDesktopService;
    private final EventDispatcher eventDispatcher;
    private final ConnectionThreadExecutor connectionThreadExecutor;
    private final VisaApiService visaApiService;

    private final MessageBroker messageBroker;

    private final List<DesktopSession> desktopSessions = new ArrayList<>();
    private final Map<String, DesktopSessionMember> desktopSessionMembers = new HashMap<>();

    private final Cancellable rttDataUpdater;

    @Inject
    public DesktopSessionService(final GuacamoleDesktopService guacamoleDesktopService,
                                 final WebXDesktopService webXDesktopService,
                                 final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance,
                                 final EventDispatcher eventDispatcher,
                                 final ConnectionThreadExecutor connectionThreadExecutor,
                                 final jakarta.enterprise.inject.Instance<VisaApiService> visaApiServiceInstance) {
        this.guacamoleDesktopService = guacamoleDesktopService;
        this.webXDesktopService = webXDesktopService;
        this.messageBroker = messageBrokerInstance.get();
        this.eventDispatcher = eventDispatcher;
        this.connectionThreadExecutor = connectionThreadExecutor;
        this.visaApiService = visaApiServiceInstance.get();

        this.messageBroker.subscribe(UserConnectedMessage.class)
            .next((message) -> this.onUserConnected(message.connectionId(), message.clientId(), message.user()));
        this.messageBroker.subscribe(UserDisconnectedMessage.class)
            .next((message) -> this.onUserDisconnected(message.connectionId(), message.clientId(), message.user()));
        this.messageBroker.subscribe(AccessRevokedMessage.class)
            .next((message) -> this.onAccessRevoked(message.connectionId(), message.userId()));
        this.messageBroker.subscribe(SessionClosedMessage.class)
            .next((message) -> this.onSessionClosed(message.connectionId()));
        this.messageBroker.subscribe(SessionLockedMessage.class)
            .next((message) -> this.onSessionLocked(message.connectionId(), message.user()));
        this.messageBroker.subscribe(SessionUnlockedMessage.class)
            .next((message) -> this.onSessionUnlocked(message.connectionId()));

        this.rttDataUpdater = Timer.setInterval(this::storeDesktopSessionConnectionStats, 60, TimeUnit.SECONDS);
    }

    @Shutdown
    public void shutdown() {
        this.rttDataUpdater.cancel();
        this.messageBroker.shutdown();
    }

    public void createDesktopSessionMember(final SocketClient client, final ConnectedUser user, final VdiInstance instance, final String activeConnectionId, final NopSender nopSender) throws OwnerNotConnectedException, UnauthorizedException, ConnectionException, ApiCommunicationException {

        final InstanceMemberRole role = user.role();
        if (role == InstanceMemberRole.NONE) {
            throw new UnauthorizedException("User " + user.fullName() + " is unauthorised to access the instance " + instance.id());
        }

        // Create RemoteDesktopConnection (guacamole or webx)
        boolean isWebX = client.protocol().equals(DesktopService.WEBX_PROTOCOL);

        logger.info("User {} creating {} desktop connection to instance {}", user, client.protocol(), instance.id());
        final RemoteDesktopConnection remoteDesktopConnection = isWebX ?
            webXDesktopService.connect(client, instance, user, activeConnectionId) :
            guacamoleDesktopService.connect(client, instance, user, activeConnectionId);

        // Get the final connectionId (may be a new one compared to the active one if a new session created)
        final String connectionId = remoteDesktopConnection.getConnectionId();

        // Get or create a new DesktopSession: use the ID of the InstanceSession
        DesktopSession desktopSession = this.getOrCreateDesktopSession(connectionId, instance, client.protocol());

        // Create session member: Add a NOP timer to keep the connection alive
        DesktopSessionMember desktopSessionMember = new DesktopSessionMember(client.clientId(), user, remoteDesktopConnection, desktopSession, nopSender);
        this.addDesktopSessionMember(desktopSession, desktopSessionMember);

        // Set up ping response handler for remote desktop
        remoteDesktopConnection.setPingResponseHandler(data -> this.onPongReceived(desktopSessionMember, data.source(), data.rttMs(), data.date()));

        // Activate idle session timer
        desktopSessionMember.idleSessionHandler().start(() -> this.onDesktopMemberIdle(desktopSessionMember));

        // Start the connection thread
        this.connectionThreadExecutor.startConnectionThread(remoteDesktopConnection.getConnectionThread());

        // Notify the server of new connection
        this.visaApiService.onClientConnection(instance.id(), client.protocol(), connectionId, client.clientId(), user);

        logger.info("After connection, currently have {} remote desktop clients (Guacamole = {}, WebX = {})",
            this.countSessionMembers(null),
            this.countSessionMembers(DesktopService.GUACAMOLE_PROTOCOL),
            this.countSessionMembers(DesktopService.WEBX_PROTOCOL));
    }

    public void onDesktopMemberDisconnect(final DesktopSessionMember desktopSessionMember) {
        // Remove, before we close the tunnel, the desktop session member (avoids double call when this is called from a Idle Timeout)
        DesktopSession desktopSession = desktopSessionMember.session();
        this.removeDesktopSessionMember(desktopSession, desktopSessionMember);

        // Stop the idle handler
        desktopSessionMember.idleSessionHandler().stop();
        desktopSessionMember.nopTimer().cancel();

        desktopSessionMember.remoteDesktopConnection().getConnectionThread().closeTunnel();

        // Notify the server of disconnection
        try {
            this.visaApiService.onClientDisconnection(desktopSession.getConnectionId(), desktopSessionMember.clientId(), desktopSessionMember.connectedUser());

        } catch (ApiCommunicationException e) {
            logger.error(e.getMessage());
        }

        logger.info("After disconnection, currently have {} remote desktop clients (Guacamole = {}, WebX = {})",
            this.countSessionMembers(null),
            this.countSessionMembers(DesktopService.GUACAMOLE_PROTOCOL),
            this.countSessionMembers(DesktopService.WEBX_PROTOCOL));

    }

    private void onDesktopMemberIdle(final DesktopSessionMember desktopSessionMember) {
        logger.warn("Idle timeout for desktop session member {}: disconnecting", desktopSessionMember);
        this.onDesktopMemberDisconnect(desktopSessionMember);
    }

    public void revokeUserAccess(final DesktopSessionMember ownerSessionMember, final String userId) {
        // Verify that we have a connection and that the user is the owner
        if (ownerSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER)) {
            // Forward reply to broker once we've verified that the revoke command comes from the owner
            this.messageBroker.broadcast(new AccessRevokedMessage(ownerSessionMember.session().getConnectionId(), userId));
        }
    }

    public void onPongReceived(final DesktopSessionMember desktopSessionMember, PingResponseData.PingSource source, long rttMs, Date date) {
        if (source.equals(PingResponseData.PingSource.SERVER)) {
            desktopSessionMember.remoteDesktopConnection().addRemoteDesktopRttMsSample(rttMs, date);
//            logger.info("DesktopSessionMember {} remote desktop RTT is {}ms", desktopSessionMember.clientId(), rttMs);

        } else {
            desktopSessionMember.remoteDesktopConnection().addClientRttSample(rttMs, date);
//            logger.info("DesktopSessionMember {} client RTT is {}ms", desktopSessionMember.clientId(), rttMs);
        }
    }


    private void storeDesktopSessionConnectionStats() {
        logger.debug("Executing instance session sample job...");
        List<VdiRttSample> samples = new ArrayList<>();
        synchronized (this.desktopSessionMembers) {
            this.desktopSessionMembers.values().forEach(desktopSessionMember -> {
                final RemoteDesktopConnection remoteDesktopConnection = desktopSessionMember.remoteDesktopConnection();
                final Pair<SampleStats, SampleStats> sampleStatsSampleStatsPair = remoteDesktopConnection.calculateRttSampleStats();

                final SampleStats clientStats = sampleStatsSampleStatsPair.getLeft();
                final SampleStats instanceStats = sampleStatsSampleStatsPair.getRight();

                Date now = new Date();
                Date firstSampleDate = Collections.min(Arrays.asList(clientStats.firstSampleDate(), instanceStats.firstSampleDate()));
                long seconds = (now.getTime() - firstSampleDate.getTime()) / 1000;
                long minutes = Math.round(seconds / 60.0);

                final DesktopSession desktopSession = desktopSessionMember.session();
                final String connectionId = desktopSession.getConnectionId();
                final String clientId = desktopSessionMember.clientId();

                if (minutes > 0) {
                    VdiRttSample desktopSessionRttSample = VdiRttSample.Builder()
                        .connectionId(connectionId)
                        .clientId(clientId)
                        .date(firstSampleDate)
                        .samplePeriodMinutes(minutes)
                        .clientMeanRttMs(clientStats.mean())
                        .clientSdRttMs(clientStats.standardDeviation())
                        .clientRttSampleCount(clientStats.sampleCount())
                        .instanceMeanRttMs(instanceStats.mean())
                        .instanceSdRttMs(instanceStats.standardDeviation())
                        .instanceRttSampleCount(instanceStats.sampleCount())
                        .build();

                    samples.add(desktopSessionRttSample);
                }

                remoteDesktopConnection.resetRttSamples();
            });
        }


        try {
            if (samples.isEmpty()) {
                logger.debug("... no samples recorded");

            } else {
                this.visaApiService.createDesktopSessionRttSamples(samples);
                logger.debug("... stored {} samples", samples.size());
            }

        } catch (ApiCommunicationException e) {
            logger.error("Failed to store {} samples: {}", samples.size(), e.getMessage());
        }
    }

    public Optional<DesktopSession> findDesktopSession(final String connectionId) {
        synchronized (this.desktopSessions) {
            return this.desktopSessions.stream()
                .filter(desktopSession -> desktopSession.getConnectionId().equals(connectionId))
                .findAny();
        }
    }

    public Optional<DesktopSessionMember> getDesktopSessionMember(final String clientId) {
        // Un-synchronized access to the map is ok since we are only reading from it and we want to go fast
        return Optional.ofNullable(this.desktopSessionMembers.get(clientId));
    }

    public void updateSessionMemberActivity(final DesktopSessionMember desktopSessionMember) throws ApiCommunicationException {
        final DesktopSession desktopSession = desktopSessionMember.session();
        final RemoteDesktopConnection remoteDesktopConnection = desktopSessionMember.remoteDesktopConnection();
        final String connectionId = desktopSession.getConnectionId();
        final String clientId = desktopSessionMember.clientId();

        this.visaApiService.updateClientActivity(desktopSessionMember.session().getInstance().id(), connectionId, clientId, desktopSessionMember.connectedUser(), remoteDesktopConnection.getLastInstanceUpdateTime(), remoteDesktopConnection.getLastInteractionAt(), remoteDesktopConnection.getInstanceActivity());
        remoteDesktopConnection.resetInstanceActivity();
    }

    private int countSessionMembers(String protocol) {
        synchronized (this.desktopSessions) {
            if (protocol == null) {
                return this.desktopSessions.stream()
                    .mapToInt(DesktopSession::memberCount)
                    .sum();

            } else {
                return this.desktopSessions.stream()
                    .filter(session -> session.getProtocol().equals(protocol))
                    .mapToInt(DesktopSession::memberCount)
                    .sum();
            }
        }
    }

    private void onUserConnected(final String connectionId, final String clientId, final ConnectedUser user) {
        this.getDesktopSession(connectionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstance().id();
            final String protocol = desktopSession.getProtocol();
            logger.info("User {} connected to instance {} with protocol {}", user, instanceId, protocol);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.clientId().equals(clientId))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), USER_CONNECTED_EVENT, new UserConnectedEvent(user, instanceId));
                });
        });
    }

    private void onUserDisconnected(final String connectionId, final String clientId, final ConnectedUser user) {
        this.getDesktopSession(connectionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstance().id();
            final String protocol = desktopSession.getProtocol();
            logger.info("User {} disconnected from instance {} with protocol {}", user, instanceId, protocol);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.clientId().equals(clientId))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), USER_DISCONNECTED_EVENT, new UserDisconnectedEvent(user, instanceId));
                });
        });
    }

    private void onAccessRevoked(final String connectionId, final String userId) {
        this.getDesktopSession(connectionId).ifPresent(desktopSession -> {
            final Long instanceId = desktopSession.getInstance().id();
            logger.info("Access revoked for user with id: {} in instance {}", userId, instanceId);

            this.eventDispatcher.sendEventToUser(userId, ACCESS_REVOKED_EVENT);

            desktopSession.filterMembers(desktopSessionMember -> desktopSessionMember.connectedUser().id().equals(userId))
                .forEach(desktopSessionMember -> {
                    logger.info("Revoking access to remote desktop for instance {} with protocol {} for user {} with client ID {}", instanceId, desktopSession.getProtocol(), desktopSessionMember.connectedUser().fullName(), desktopSessionMember.clientId());
                    desktopSessionMember.disconnect();
                });
        });
    }

    private void onSessionClosed(final String connectionId) {
        this.getDesktopSession(connectionId).ifPresent(desktopSession -> {
            logger.info("Session closed (owner away) for instance {} with protocol {}: disconnecting all clients", desktopSession.getInstance().id(), desktopSession.getProtocol());

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), ACCESS_REVOKED_EVENT);
                    desktopSessionMember.disconnect();
                });
        });
    }

    private void onSessionLocked(final String connectionId, final ConnectedUser user) {
        this.getDesktopSession(connectionId).ifPresent(desktopSession -> {
            logger.info("Session locked (owner away) for instance {} with protocol {}: making all clients read-only", desktopSession.getInstance().id(), desktopSession.getProtocol());
            desktopSession.setLocked(true);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    final VdiInstance instance = desktopSession.getInstance();
                    if (desktopSessionMember.connectedUser().role().equals(InstanceMemberRole.SUPPORT) && instance.canConnectWhileOwnerAway()) {
                        this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), USER_DISCONNECTED_EVENT, new UserDisconnectedEvent(user, instance.id()));

                    } else {
                        this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), SESSION_LOCKED_EVENT);
                    }

                });
        });
    }

    private void onSessionUnlocked(final String connectionId) {
        this.getDesktopSession(connectionId).ifPresent(desktopSession -> {
            logger.info("Session unlocked (owner back) for instance {} with protocol {}: all clients resume original roles", desktopSession.getInstance().id(), desktopSession.getProtocol());
            desktopSession.setLocked(false);

            desktopSession.filterMembers(desktopSessionMember -> !desktopSessionMember.connectedUser().role().equals(InstanceMemberRole.OWNER))
                .forEach(desktopSessionMember -> {
                    this.eventDispatcher.sendEventToClient(desktopSessionMember.clientId(), SESSION_UNLOCKED_EVENT);
                });
        });
    }


    private void removeDesktopSession(final DesktopSession desktopSession) {
        synchronized (this.desktopSessionMembers) {
            this.desktopSessions.removeIf(item -> item.equals(desktopSession));
        }
    }

    private void addDesktopSessionMember(final DesktopSession desktopSession, final DesktopSessionMember desktopSessionMember) {
        synchronized (this.desktopSessionMembers) {
            desktopSession.addMember(desktopSessionMember);
            this.desktopSessionMembers.put(desktopSessionMember.clientId(), desktopSessionMember);
        }
    }

    private void removeDesktopSessionMember(final DesktopSession desktopSession, final DesktopSessionMember desktopSessionMember) {
        synchronized (this.desktopSessionMembers) {
            this.desktopSessionMembers.remove(desktopSessionMember.clientId());
            desktopSession.removeMember(desktopSessionMember);
            if (desktopSession.getMembers().isEmpty()) {
                this.removeDesktopSession(desktopSession);
            }
        }
    }

    private Optional<DesktopSession> getDesktopSession(final String connectionId) {
        synchronized (this.desktopSessions) {
            return this.desktopSessions.stream().filter(desktopSession -> desktopSession.getConnectionId().equals(connectionId)).findAny();
        }
    }

    private DesktopSession getOrCreateDesktopSession(final String connectionId, final VdiInstance instance, final String protocol) {
        synchronized (this.desktopSessions) {
            DesktopSession desktopSession = this.desktopSessions.stream()
                .filter(aSession -> aSession.getConnectionId().equals(connectionId))
                .findFirst()
                .orElse(null);

            if (desktopSession == null) {
                desktopSession = new DesktopSession(connectionId, instance, protocol);
                this.desktopSessions.add(desktopSession);
            };
            return desktopSession;
        }
    }
}
