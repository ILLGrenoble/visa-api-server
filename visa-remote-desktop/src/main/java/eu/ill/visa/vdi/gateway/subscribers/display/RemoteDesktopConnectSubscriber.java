package eu.ill.visa.vdi.gateway.subscribers.display;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.exceptions.ConnectionException;
import eu.ill.visa.vdi.domain.exceptions.OwnerNotConnectedException;
import eu.ill.visa.vdi.domain.exceptions.UnauthorizedException;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.EventChannel;
import eu.ill.visa.vdi.domain.models.NopSender;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.SocketConnectSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.ill.visa.vdi.domain.models.SessionEvent.ACCESS_DENIED;
import static eu.ill.visa.vdi.domain.models.SessionEvent.OWNER_AWAY_EVENT;

public class RemoteDesktopConnectSubscriber implements SocketConnectSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopConnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;
    private final InstanceService instanceService;
    private final InstanceSessionService instanceSessionService;

    public RemoteDesktopConnectSubscriber(final DesktopSessionService desktopSessionService,
                                          final DesktopAccessService desktopAccessService,
                                          final InstanceService instanceService,
                                          final InstanceSessionService instanceSessionService) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
    }

    @Override
    public void onConnect(final SocketClient socketClient, final NopSender nopSender) {

        logger.info("Initialising websocket client for RemoteDesktopConnection with token {}", socketClient.token());

        this.desktopSessionService.getPendingDesktopSessionMember(socketClient.token()).ifPresentOrElse(pendingDesktopSessionMember -> {
            this.desktopSessionService.removePendingDesktopSessionMember(pendingDesktopSessionMember);

            final Long instanceId = pendingDesktopSessionMember.instanceId();
            final Instance instance = this.instanceService.getFullById(instanceId);
            if (instance != null) {
                final ConnectedUser user = pendingDesktopSessionMember.connectedUser();
                final EventChannel eventChannel = pendingDesktopSessionMember.eventChannel();
                try {
                    if (instance.getUsername() == null) {
                        logger.warn("No username is associated with the instance {}: the owner has never connected. Disconnecting user {}", instance.getId(), user);
                        throw new OwnerNotConnectedException();

                    } else {
                        if (user.getRole().equals(InstanceMemberRole.SUPPORT)) {
                            // See if user can connect even if owner is away
                            if (this.instanceSessionService.canConnectWhileOwnerAway(instance, user.getId())) {
                                this.desktopSessionService.createDesktopSessionMember(socketClient, pendingDesktopSessionMember);

                            } else {
                                final InstanceSession instanceSession = this.instanceSessionService.getByInstanceAndProtocol(instance, pendingDesktopSessionMember.protocol());
                                if (instanceSession != null && this.desktopSessionService.isOwnerConnected(instanceSession.getId())) {
                                    // Start process of requesting access from the owner
                                    this.desktopAccessService.requestAccess(socketClient, instanceSession.getId(), pendingDesktopSessionMember, nopSender);

                                } else {
                                    throw new OwnerNotConnectedException();
                                }
                            }

                        } else {
                            this.desktopSessionService.createDesktopSessionMember(socketClient, pendingDesktopSessionMember);
                        }
                    }

                } catch (OwnerNotConnectedException exception) {
                    eventChannel.sendEvent(OWNER_AWAY_EVENT);
                    eventChannel.disconnect();
                    socketClient.disconnect();

                } catch (UnauthorizedException exception) {
                    logger.warn(exception.getMessage());
                    eventChannel.sendEvent(ACCESS_DENIED);
                    eventChannel.disconnect();
                    socketClient.disconnect();

                } catch (ConnectionException exception) {
                    logger.error(exception.getMessage());
                    eventChannel.sendEvent(ACCESS_DENIED);
                    eventChannel.disconnect();
                    socketClient.disconnect();
                }

            } else {
                logger.error("Instance no longer exists for token {}", socketClient.token());
                socketClient.disconnect();
            }

        }, () -> {
            logger.error("Failed to find pending desktop session connection for token {}", socketClient.token());
            socketClient.disconnect();
        });
    }
}
