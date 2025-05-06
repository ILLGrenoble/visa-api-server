package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.DesktopSession;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class RemoteDesktopEventSubscriber<T> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopEventSubscriber.class);
    private static final int INSTANCE_SESSION_UPDATE_TIME_MS = 15000;

    private final DesktopSessionService desktopSessionService;
    private final int maxInactivityTimeMinutes;

    public RemoteDesktopEventSubscriber(final DesktopSessionService desktopSessionService,
                                        final int maxInactivityTimeMinutes) {
        this.desktopSessionService = desktopSessionService;
        this.maxInactivityTimeMinutes = maxInactivityTimeMinutes;
    }

    public void onEvent(final SocketClient socketClient, final T data) {
        this.desktopSessionService.getDesktopSessionMember(socketClient.clientId()).ifPresent(desktopSessionMember -> {

            // Reset the idle handler
            desktopSessionMember.idleSessionHandler().reset();

            final DesktopSession desktopSession = desktopSessionMember.session();

            final RemoteDesktopConnection remoteDesktopConnection = desktopSessionMember.remoteDesktopConnection();

            // Get activity type (returns null for anything other than mouse or keyboard activity)
            InstanceActivityType controlActivityType = this.getControlActivityType(data);

            // Update the instance activity if mouse/keyboard event has been sent to the server
            if (controlActivityType != null) {
                remoteDesktopConnection.setInstanceActivity(controlActivityType);
            }

            InstanceMemberRole role = desktopSessionMember.connectedUser().getRole();
            if (controlActivityType == null || role.equals(InstanceMemberRole.OWNER) || role.equals(InstanceMemberRole.SUPPORT) || (role.equals(InstanceMemberRole.USER) && !desktopSession.isLocked())) {
                this.writeData(remoteDesktopConnection.getConnectionThread(), data);
            }

            // Two things to do:
            //  1: update instance last seen at (so that it isn't deleted): happens for any remote desktop event (ie desktop open)
            //  2: update instance last interaction at (to determine when sessions are active): happens only when the remote desktop has user keyboard or mouse interaction
            final Date lastInstanceUpdateTime = remoteDesktopConnection.getLastInstanceUpdateTime();
            final Date currentDate = new Date();

            if (lastInstanceUpdateTime == null || (currentDate.getTime() - lastInstanceUpdateTime.getTime() >= INSTANCE_SESSION_UPDATE_TIME_MS)) {
                remoteDesktopConnection.setLastInstanceUpdateTime(currentDate);

                // Use virtual thread to update interaction times so that the websocket can return ASAP
                Thread.startVirtualThread(() -> {
                    try {
                        // Determine if the instance is still being actively used by the user
                        long timeSinceLastInteractionMs = currentDate.getTime() - remoteDesktopConnection.getLastInteractionAt().getTime();
                        long timeSinceLastInteractionMinutes = TimeUnit.MINUTES.convert(timeSinceLastInteractionMs, TimeUnit.MILLISECONDS);
                        if (timeSinceLastInteractionMinutes >= maxInactivityTimeMinutes) {
                            logger.info("{} has been inactive for {} minutes: disconnecting the user", desktopSessionMember, timeSinceLastInteractionMinutes);
                            desktopSessionMember.remoteDesktopConnection().getClient().disconnect();

                        } else {
                            // Update database values
                            this.desktopSessionService.updateSessionMemberActivity(desktopSessionMember);
                        }

                    } catch (Exception error) {
                        logger.error("Failed to update instance {} activity: {}", desktopSession.getInstanceId(), error.getMessage());
                    }
                });
            }
       });
    }

    protected abstract InstanceActivityType getControlActivityType(T data);

    protected abstract void writeData(ConnectionThread connectionThread, T data);
}
