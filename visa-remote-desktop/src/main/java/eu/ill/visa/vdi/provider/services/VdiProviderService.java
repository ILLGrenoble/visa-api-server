package eu.ill.visa.vdi.provider.services;


import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.business.services.*;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.partial.InstancePartial;
import eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.broker.*;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.gateway.events.UsersConnectedEvent;
import eu.ill.visa.vdi.provider.exceptions.SessionConnectionException;
import eu.ill.visa.vdi.provider.models.VdiAuthenticationToken;
import eu.ill.visa.vdi.provider.models.VdiInstance;
import eu.ill.visa.vdi.provider.models.VdiRttSample;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.ill.visa.vdi.domain.models.SessionEvent.USERS_CONNECTED_EVENT;
import static java.util.Objects.requireNonNullElse;

@Transactional
@ApplicationScoped
public class VdiProviderService {

    private static final Logger logger = LoggerFactory.getLogger(VdiProviderService.class);

    private final UserService userService;
    private final InstanceService instanceService;
    private final InstanceAuthenticationTokenService instanceAuthenticationTokenService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceSessionMemberService instanceSessionMemberService;
    private final ImageProtocolService imageProtocolService;
    private final InstanceExpirationService instanceExpirationService;
    private final InstanceActivityService instanceActivityService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;
    private final DesktopSessionRttSampleService desktopSessionRttSampleService;
    private final EventDispatcher eventDispatcher;

    private final MessageBroker messageBroker;

    @Inject
    public VdiProviderService(final UserService userService,
                              final InstanceService instanceService,
                              final InstanceAuthenticationTokenService instanceAuthenticationTokenService,
                              final InstanceSessionService instanceSessionService,
                              final InstanceSessionMemberService instanceSessionMemberService,
                              final ImageProtocolService imageProtocolService,
                              final InstanceExpirationService instanceExpirationService,
                              final InstanceActivityService instanceActivityService,
                              final VirtualDesktopConfiguration virtualDesktopConfiguration,
                              final DesktopSessionRttSampleService desktopSessionRttSampleService,
                              final EventDispatcher eventDispatcher,
                              final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance) {
        this.userService = userService;
        this.instanceService = instanceService;
        this.instanceAuthenticationTokenService = instanceAuthenticationTokenService;
        this.instanceSessionService = instanceSessionService;
        this.instanceSessionMemberService = instanceSessionMemberService;
        this.imageProtocolService = imageProtocolService;
        this.instanceExpirationService = instanceExpirationService;
        this.instanceActivityService = instanceActivityService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
        this.desktopSessionRttSampleService = desktopSessionRttSampleService;
        this.eventDispatcher = eventDispatcher;
        this.messageBroker = messageBrokerInstance.get();
    }


    public VdiAuthenticationToken authenticate(String protocol, String token) throws InvalidTokenException {
        // Validate the token and obtain instance and user information
        InstanceAuthenticationToken instanceAuthenticationToken = this.instanceAuthenticationTokenService.authenticate(token);

        final User user = instanceAuthenticationToken.getUser();
        final Instance instance = instanceAuthenticationToken.getInstance();
        final String publicAccessToken = instanceAuthenticationToken.getPublicAccessToken();

        // Determine role of connected person
        InstanceMemberRole role = instanceSessionService.getUserSessionRole(instance, user);
        boolean usingPublicAccessToken = false;

        // Check if we should examine the accessToken
        if (role.equals(InstanceMemberRole.NONE) || role.equals(InstanceMemberRole.SUPPORT) && publicAccessToken != null && publicAccessToken.equals(instance.getPublicAccessToken())) {
            logger.info("User {} is requesting to connect using a public access token for instance {}", user, instance.getId());
            role = instance.getPublicAccessRole();
            usingPublicAccessToken = true;
        }

        ConnectedUser connectedUser = new ConnectedUser(user.getId(), user.getFullName(), role);

        final Integer port = this.getProtocolPort(protocol);
        final InstanceSession instanceSession = this.instanceSessionService.getLatestByInstanceAndProtocol(instance, protocol);
        final boolean canConnectWhileOwnerAway = this.instanceSessionService.canConnectWhileOwnerAway(instance, user);
        final boolean ownerIsConnected = this.instanceSessionMemberService.isOwnerConnected(instance, protocol);
        final InstanceMember owner = instance.getOwner();
        final boolean ownerIsExternalUser = !owner.getUser().hasRoleWithName(Role.STAFF_ROLE);

        final String activeConnectionId = instanceSession != null ? instanceSession.getConnectionId() : null;
        final VdiInstance vdiInstance = new VdiInstance(instance, port, canConnectWhileOwnerAway, ownerIsExternalUser);


        return new VdiAuthenticationToken(vdiInstance, connectedUser, usingPublicAccessToken, activeConnectionId, ownerIsConnected);
    }

    public void onSessionConnect(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser user) throws SessionConnectionException {
        // Get latest session for instance and protocol
        InstanceSession instanceSession = instanceSessionService.getLatestByInstanceIdAndProtocol(instanceId, protocol);

        // Check if one exists and compare the connectionId
        if (instanceSession != null && !instanceSession.getConnectionId().equals(connectionId)) {
            // Connection Id differs so remove the old one
            instanceSession.setCurrent(false);
            this.instanceSessionService.save(instanceSession);
            logger.info("Instance session {} for User {} is no longer valid: removing it", instanceSession.getConnectionId(), getInstanceAndUser(instanceId, user));

            instanceSession = null;
        }

        if (instanceSession == null) {
            instanceSession = instanceSessionService.create(instanceId, protocol, connectionId);
            logger.info("User {} created {} session {}", getInstanceAndUser(instanceId, user), protocol, instanceSession.getConnectionId());
        }

        boolean unlockSession = virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)
            && user.getRole().equals(InstanceMemberRole.OWNER)
            && !this.isOwnerConnected(instanceSession.getId());

        // Update the connected clients of the session
        this.instanceSessionMemberService.create(instanceSession, clientId, this.userService.getById(user.getId()), user.getRole());

        // Remove instance from instance_expiration table if it is there due to inactivity
        this.instanceExpirationService.onInstanceActivated(instanceId);

        if (unlockSession) {
            // Unlock session for all clients
            this.unlockSession(instanceSession);

        } else {
            this.connectUser(instanceSession, clientId, user);
        }
    }

    public void onSessionDisconnect(final String connectionId, final String clientId, final ConnectedUser user) {
        InstanceSession session = instanceSessionService.getLatestByConnectionId(connectionId);
        if (session != null) {
            // Remove client/member from instance session
            instanceSessionService.deleteSessionMember(session, clientId);

            if (user.getRole().equals(InstanceMemberRole.OWNER) && !this.isOwnerConnected(session.getId())) {
                if (this.virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM)) {
                    this.lockSession(session, user);

                } else {
                    logger.info("There is no owner ar admin connected so all clients will be disconnected");
                    this.closeSession(session);
                }

            } else {
                // broadcast events for a user disconnected and current users
                this.disconnectUser(session, clientId, user);
            }
        }
    }

    public void onSessionFailure(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser user) {
        logger.warn("Failed to connect {} to given {} session {} so creating a new one", getInstanceAndUser(instanceId, user), protocol, connectionId);
        // Session connection failed so invalidate it
        InstanceSession session = instanceSessionService.getLatestByConnectionId(connectionId);
        session.setCurrent(false);
        this.instanceSessionService.updatePartial(session);
    }

    public void onSessionActivity(final Long instanceId, final String connectionId, final String clientId, final ConnectedUser user, final Date lastSeenAt, final Date lastInteractionAt, final InstanceActivityType instanceActivityType) {

        final InstancePartial instance = this.instanceService.getPartialById(instanceId);
        if (instance != null) {
            // Update instance
            instance.setLastSeenAt(lastSeenAt);
            instance.setLastInteractionAt(lastInteractionAt);
            instanceService.updatePartial(instance);

            final InstanceSessionMemberPartial instanceSessionMember = this.instanceSessionMemberService.getPartialByConnectionIdAndClientId(connectionId, clientId);
            if (instanceSessionMember != null) {
                instanceSessionMember.setLastInteractionAt(lastInteractionAt);
                instanceSessionMemberService.updateInteractionAt(instanceSessionMember);

                if (instanceActivityType != null) {
                    this.instanceActivityService.create(instanceSessionMember.getUserId(), instanceSessionMember.getInstanceId(), instanceActivityType);
                }
            }
        }
    }

    public void onSessionRttData(List<VdiRttSample> samples) {
        List<DesktopSessionRttSample> sessionRttSamples = new ArrayList<>();
        for (VdiRttSample sample : samples) {
            final InstanceSessionMemberPartial instanceSessionMember = this.instanceSessionMemberService.getPartialByConnectionIdAndClientId(sample.connectionId(), sample.clientId());
            if (instanceSessionMember != null) {
                DesktopSessionRttSample desktopSessionRttSample = DesktopSessionRttSample.Builder()
                    .instanceSessionMemberId(instanceSessionMember.getId())
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

    private void connectUser(final InstanceSession instanceSession, final String clientId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserConnectedMessage(instanceSession.getConnectionId(), clientId, user));
        this.sendUsersConnectedEvent(instanceSession);
    }

    private void disconnectUser(final InstanceSession instanceSession, final String clientId, final ConnectedUser user) {
        this.messageBroker.broadcast(new UserDisconnectedMessage(instanceSession.getConnectionId(), clientId, user));
        this.sendUsersConnectedEvent(instanceSession);
    }

    private void closeSession(final InstanceSession instanceSession) {
        this.messageBroker.broadcast(new SessionClosedMessage(instanceSession.getConnectionId()));
    }

    private void lockSession(final InstanceSession instanceSession, final ConnectedUser user) {
        this.messageBroker.broadcast(new SessionLockedMessage(instanceSession.getConnectionId(), user));
        this.sendUsersConnectedEvent(instanceSession);
    }

    private void unlockSession(final InstanceSession instanceSession) {
        this.messageBroker.broadcast(new SessionUnlockedMessage(instanceSession.getConnectionId()));
        this.sendUsersConnectedEvent(instanceSession);
    }

    private void sendUsersConnectedEvent(final InstanceSession instanceSession) {
        final String protocol = instanceSession.getProtocol();

        List<InstanceSessionMemberPartial> instanceSessionMembers = this.instanceSessionMemberService.getAllPartialsByInstanceSessionId(instanceSession.getId());

        List<ConnectedUser> users = this.convertToConnectedUsers(instanceSession, instanceSessionMembers).stream().distinct().toList();
        if (!users.isEmpty()) {
            logger.info("Instance {} with protocol {} has the following users connected: {}", instanceSession.getId(), protocol, users.stream().map(ConnectedUser::toString).toList());

            instanceSessionMembers.forEach(member -> {
                this.eventDispatcher.sendEventToClient(member.getClientId(), USERS_CONNECTED_EVENT, new UsersConnectedEvent(users, instanceSession.getInstanceId()));
            });
        }
    }

    private List<ConnectedUser> convertToConnectedUsers(final InstanceSession instanceSession, final List<InstanceSessionMemberPartial> instanceSessionMembers) {
        logger.info("Instance {} has {} connected users", instanceSession.getInstanceId(), instanceSessionMembers.size());

        final boolean ownerIsConnected = this.isOwnerConnected(instanceSession.getId());
        boolean sessionLocked = !ownerIsConnected && virtualDesktopConfiguration.ownerDisconnectionPolicy().equals(VirtualDesktopConfiguration.OWNER_DISCONNECTION_POLICY_LOCK_ROOM);

        return instanceSessionMembers.stream().map(instanceSessionMember -> {
            InstanceMemberRole role = instanceSessionMember.getRole();
            if (sessionLocked && role.equals(InstanceMemberRole.USER)) {
                role= InstanceMemberRole.GUEST;
            }
            return new ConnectedUser(instanceSessionMember.getUserId(), instanceSessionMember.getUserFullName(), role);
        }).toList();
    }

    private boolean isOwnerConnected(final Long sessionId) {
        List<InstanceSessionMemberPartial> instanceSessionMembers = this.instanceSessionMemberService.getAllPartialsByInstanceSessionId(sessionId);
        return instanceSessionMembers.stream().anyMatch(instanceSessionMember -> instanceSessionMember.getRole().equals(InstanceMemberRole.OWNER));
    }

    private String getInstanceAndUser(Long instanceId, ConnectedUser user) {
        return "User " + user.getFullName() + " (" + user.getId() + ", " + user.getRole().toString() + "), Instance " + instanceId;
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
}
