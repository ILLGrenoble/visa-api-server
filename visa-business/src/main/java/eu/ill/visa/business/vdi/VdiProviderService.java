package eu.ill.visa.business.vdi;


import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.business.VDIConfiguration;
import eu.ill.visa.business.services.*;
import eu.ill.visa.business.vdi.events.UsersConnectedEvent;
import eu.ill.visa.common.vdi.apiconnect.exceptions.InvalidTokenException;
import eu.ill.visa.common.vdi.broker.*;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import eu.ill.visa.common.vdi.domain.enums.InstanceActivityType;
import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.partial.InstancePartial;
import eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial;
import eu.ill.visa.persistence.models.Tuple2;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.ill.visa.business.vdi.events.SessionEvent.USERS_CONNECTED_EVENT;
import static java.util.Objects.requireNonNullElse;

@Transactional
@ApplicationScoped
public class VdiProviderService {

    private static final Logger logger = LoggerFactory.getLogger(VdiProviderService.class);

    private final UserService userService;
    private final InstanceService instanceService;
    private final InstanceAuthenticationTokenService instanceAuthenticationTokenService;
    private final InstanceDesktopSessionService instanceDesktopSessionService;
    private final ImageProtocolService imageProtocolService;
    private final InstanceExpirationService instanceExpirationService;
    private final InstanceActivityService instanceActivityService;
    private final VDIConfiguration vdiConfiguration;
    private final DesktopSessionRttSampleService desktopSessionRttSampleService;
    private final EventDispatcher eventDispatcher;

    private final MessageBroker messageBroker;

    @Inject
    public VdiProviderService(final UserService userService,
                              final InstanceService instanceService,
                              final InstanceAuthenticationTokenService instanceAuthenticationTokenService,
                              final InstanceDesktopSessionService instanceDesktopSessionService,
                              final ImageProtocolService imageProtocolService,
                              final InstanceExpirationService instanceExpirationService,
                              final InstanceActivityService instanceActivityService,
                              final VDIConfiguration vdiConfiguration,
                              final DesktopSessionRttSampleService desktopSessionRttSampleService,
                              final EventDispatcher eventDispatcher,
                              final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance) {
        this.userService = userService;
        this.instanceService = instanceService;
        this.instanceAuthenticationTokenService = instanceAuthenticationTokenService;
        this.instanceDesktopSessionService = instanceDesktopSessionService;
        this.imageProtocolService = imageProtocolService;
        this.instanceExpirationService = instanceExpirationService;
        this.instanceActivityService = instanceActivityService;
        this.vdiConfiguration = vdiConfiguration;
        this.desktopSessionRttSampleService = desktopSessionRttSampleService;
        this.eventDispatcher = eventDispatcher;
        this.messageBroker = messageBrokerInstance.get();
    }

    @Startup
    public void startup() {
        if (this.vdiConfiguration.cleanupSessionsOnStartup()) {
            this.cleanupSessions();
        }
    }

    public VdiAuthenticationToken authenticate(String protocol, String token) throws InvalidTokenException {
        // Validate the token and obtain instance and user information
        InstanceAuthenticationToken instanceAuthenticationToken = this.instanceAuthenticationTokenService.authenticate(token);

        final User user = instanceAuthenticationToken.getUser();
        final Instance instance = instanceAuthenticationToken.getInstance();
        final String publicAccessToken = instanceAuthenticationToken.getPublicAccessToken();

        // Determine role of connected person
        InstanceMemberRole role = instanceDesktopSessionService.getUserSessionRole(instance, user);
        boolean usingPublicAccessToken = false;

        // Check if we should examine the accessToken
        if (role.equals(InstanceMemberRole.NONE) || role.equals(InstanceMemberRole.SUPPORT) && publicAccessToken != null && publicAccessToken.equals(instance.getPublicAccessToken())) {
            logger.info("User {} is requesting to connect using a public access token for instance {}", user, instance.getId());
            role = instance.getPublicAccessRole();
            usingPublicAccessToken = true;
        }

        ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

        final Integer port = this.getProtocolPort(protocol);
        final InstanceDesktopSession instanceDesktopSession = this.instanceDesktopSessionService.getLatestByInstanceId(instance.getId());
        final boolean canConnectWhileOwnerAway = this.instanceDesktopSessionService.canConnectWhileOwnerAway(instance, user);
        final InstanceMember owner = instance.getOwner();
        final boolean ownerIsExternalUser = !owner.getUser().hasRoleWithName(Role.STAFF_ROLE);

        final String activeConnectionId = instanceDesktopSession != null ? instanceDesktopSession.getConnectionId() : null;
        final boolean ownerIsConnected = this.instanceDesktopSessionService.isOwnerConnected(activeConnectionId);

        final VdiInstance vdiInstance = this.buildVdiInstance(instance, port, canConnectWhileOwnerAway, ownerIsExternalUser);

        return new VdiAuthenticationToken(vdiInstance, connectedUser, usingPublicAccessToken, activeConnectionId, ownerIsConnected);
    }

    public void onSessionConnect(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser user) {

        // Create a new InstanceDesktopSession
        final InstanceDesktopSession instanceDesktopSession = instanceDesktopSessionService.create(connectionId, clientId, instanceId, user.id(), user.role(), protocol);
        logger.info("User {} created {} session {}", getInstanceAndUser(instanceId, user), protocol, connectionId);

        boolean unlockSession = vdiConfiguration.ownerDisconnectionPolicy().equals(VDIConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)
            && user.role().equals(InstanceMemberRole.OWNER)
            && !instanceDesktopSessionService.isOwnerConnected(connectionId);

        // Remove instance from instance_expiration table if it is there due to inactivity
        this.instanceExpirationService.onInstanceActivated(instanceId);

        if (unlockSession) {
            // Unlock session for all clients
            this.unlockSession(connectionId, instanceId);

        } else {
            this.connectUser(connectionId, clientId, instanceId, user);
        }
    }

    public void onSessionDisconnect(final String connectionId, final String clientId, final ConnectedUser user) {
        final InstanceDesktopSession instanceDesktopSession = this.instanceDesktopSessionService.getByConnectionIdAndClientId(connectionId, clientId);
        if (instanceDesktopSession != null) {
            final Long instanceId = instanceDesktopSession.getInstanceId();
            // Close the instance desktop session
            this.instanceDesktopSessionService.close(instanceDesktopSession);

            if (user.role().equals(InstanceMemberRole.OWNER) && !this.instanceDesktopSessionService.isOwnerConnected(connectionId)) {
                if (this.vdiConfiguration.ownerDisconnectionPolicy().equals(VDIConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)) {
                    this.lockSession(connectionId, instanceId, user);

                } else {
                    logger.info("There is no owner ar admin connected so all clients will be disconnected");
                    this.closeSession(connectionId, instanceId);
                }

            } else {
                // broadcast events for a user disconnected and current users
                this.disconnectUser(connectionId, clientId, instanceId, user);
            }
        }
    }

    public void onSessionFailure(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser user) {
        logger.warn("Failed to connect {} to given {} session {}", getInstanceAndUser(instanceId, user), protocol, connectionId);
        // Session connection failed so invalidate it
        final InstanceDesktopSession instanceDesktopSession = this.instanceDesktopSessionService.getByConnectionIdAndClientId(connectionId, clientId);
        this.instanceDesktopSessionService.close(instanceDesktopSession);
    }

    public void onSessionActivity(final Long instanceId, final String connectionId, final String clientId, final ConnectedUser user, final Date lastSeenAt, final Date lastInteractionAt, final InstanceActivityType instanceActivityType) {

        final InstanceDesktopSession instanceDesktopSession = this.instanceDesktopSessionService.getByConnectionIdAndClientId(connectionId, clientId);
        if (instanceDesktopSession != null) {
            this.instanceDesktopSessionService.updateInteractionAt(instanceDesktopSession);
        }

        if (instanceActivityType != null) {
            this.instanceActivityService.create(user.id(), instanceId, instanceActivityType);
        }

        final InstancePartial instance = this.instanceService.getPartialById(instanceId);
        if (instance != null) {
            // Update instance
            instance.setLastSeenAt(lastSeenAt);
            instance.setLastInteractionAt(lastInteractionAt);
            instanceService.updatePartial(instance);
        }
    }

    public void onSessionRttData(List<VdiRttSample> samples) {
        List<DesktopSessionRttSample> sessionRttSamples = new ArrayList<>();
        for (VdiRttSample sample : samples) {
            final InstanceDesktopSession instanceDesktopSession = this.instanceDesktopSessionService.getByConnectionIdAndClientId(sample.connectionId(), sample.clientId());

            if (instanceDesktopSession != null) {
                DesktopSessionRttSample desktopSessionRttSample = DesktopSessionRttSample.Builder()
                    .instanceSessionMemberId(instanceDesktopSession.getId())
                    .date(sample.date())
                    .samplePeriodMinutes(sample.samplePeriodMinutes())
                    .clientMeanRttMs(sample.clientMeanRttMs())
                    .clientSdRttMs(sample.clientSdRttMs())
                    .clientRttSampleCount(sample.clientRttSampleCount())
                    .instanceMeanRttMs(sample.instanceMeanRttMs())
                    .instanceSdRttMs(sample.instanceSdRttMs())
                    .instanceRttSampleCount(sample.instanceRttSampleCount())
                    .build();
                sessionRttSamples.add(desktopSessionRttSample);
            }
        }
        for (DesktopSessionRttSample desktopSessionRttSample : sessionRttSamples) {
            this.desktopSessionRttSampleService.save(desktopSessionRttSample);
        }
    }

    private void connectUser(final String connectionId, final String clientId, final Long instanceId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserConnectedMessage(connectionId, clientId, user));
        this.sendUsersConnectedEvent(connectionId, instanceId);
    }

    private void disconnectUser(final String connectionId, final String clientId, final Long instanceId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserDisconnectedMessage(connectionId, clientId, user));
        this.sendUsersConnectedEvent(connectionId, instanceId);
    }

    private void closeSession(final String connectionId, final Long instanceId) {
        this.messageBroker.broadcast(new SessionClosedMessage(connectionId));
    }

    private void lockSession(final String connectionId, final Long instanceId, final ConnectedUser user) {
        this.messageBroker.broadcast(new SessionLockedMessage(connectionId, user));
        this.sendUsersConnectedEvent(connectionId, instanceId);
    }

    private void unlockSession(final String connectionId, final Long instanceId) {
        this.messageBroker.broadcast(new SessionUnlockedMessage(connectionId));
        this.sendUsersConnectedEvent(connectionId, instanceId);
    }

    private void sendUsersConnectedEvent(final String connectionId, final Long instanceId) {
        List<Tuple2<InstanceDesktopSession, ConnectedUser>> instanceDesktopSessions = this.instanceDesktopSessionService.getAllWithConnectedUserByConnectionId(connectionId);
        logger.info("Instance {} has {} connected users", instanceId, instanceDesktopSessions.size());


        List<ConnectedUser> users = this.convertToConnectedUsers(connectionId, instanceDesktopSessions).stream().distinct().toList();
        if (!users.isEmpty()) {
            logger.info("Instance {} has the following users connected: {}", instanceId, users.stream().map(ConnectedUser::toString).toList());

            instanceDesktopSessions.forEach(tuple -> {
                final InstanceDesktopSession instanceDesktopSession = tuple.x();
                this.eventDispatcher.sendEventToClient(instanceDesktopSession.getClientId(), USERS_CONNECTED_EVENT, new UsersConnectedEvent(users, instanceId));
            });
        }
    }

    private List<ConnectedUser> convertToConnectedUsers(final String connectionId, final List<Tuple2<InstanceDesktopSession, ConnectedUser>> instanceDesktopSessions) {
        final boolean ownerIsConnected = this.instanceDesktopSessionService.isOwnerConnected(connectionId);
        boolean sessionLocked = !ownerIsConnected && vdiConfiguration.ownerDisconnectionPolicy().equals(VDIConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM);

        return instanceDesktopSessions.stream().map(tuple -> {
            final InstanceDesktopSession instanceDesktopSession = tuple.x();
            final ConnectedUser connectedUser = tuple.y();
            InstanceMemberRole role = instanceDesktopSession.getRole();
            if (sessionLocked && role.equals(InstanceMemberRole.USER)) {
                role = InstanceMemberRole.GUEST;
            }
            return new ConnectedUser(connectedUser.id(), connectedUser.fullName(), role);
        }).toList();
    }

    private String getInstanceAndUser(Long instanceId, ConnectedUser user) {
        return "User " + user.fullName() + " (" + user.id() + ", " + user.role().toString() + "), Instance " + instanceId;
    }

    private Integer getProtocolPort(final String protocol) {
        if (protocol.equals(InstanceSession.WEBX_PROTOCOL)) {
            final ImageProtocol imageProtocol = requireNonNullElse(
                imageProtocolService.getByName("WEBX"),
                new ImageProtocol("WEBX", 5555)
            );

            return imageProtocol.getPort();

        } else if (protocol.equals(InstanceSession.GUACAMOLE_PROTOCOL)) {
            final ImageProtocol imageProtocol = requireNonNullElse(
                imageProtocolService.getByName("GUACD"),
                new ImageProtocol("GUACD", 4822)
            );

            return imageProtocol.getPort();

        } else {
            return null;
        }
    }

    private VdiInstance buildVdiInstance(final Instance instance, final Integer port, final boolean canConnectWhileOwnerAway, final boolean ownerIsExternalUser) {
        final ImageProtocol secondaryVdiProtocol = instance.getPlan().getImage().getSecondaryVdiProtocol();

        return new VdiInstance(
            instance.getId(),
            instance.getScreenWidth(),
            instance.getScreenHeight(),
            instance.getKeyboardLayout(),
            instance.getUsername(),
            instance.getPlan().getImage().getAutologin(),
            instance.getIpAddress(),
            port,
            secondaryVdiProtocol != null ? secondaryVdiProtocol.getName() : null,
            canConnectWhileOwnerAway,
            ownerIsExternalUser
        );
    }

    private void cleanupSessions() {
        this.instanceDesktopSessionService.closeAllSessions();
    }

}
