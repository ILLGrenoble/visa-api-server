package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.partial.InstancePartial;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.DesktopSession;
import eu.ill.visa.vdi.domain.models.RemoteDesktopConnection;
import eu.ill.visa.vdi.domain.models.SocketClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public abstract class RemoteDesktopEventSubscriber<T> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopEventSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final InstanceService instanceService;

    public RemoteDesktopEventSubscriber(final DesktopSessionService desktopSessionService,
                                        final InstanceService instanceService) {
        this.desktopSessionService = desktopSessionService;
        this.instanceService = instanceService;
    }

    public void onEvent(final SocketClient socketClient, final T data) {
        this.desktopSessionService.findDesktopSessionMemberByClientId(socketClient.clientId()).ifPresent(desktopSessionMember -> {

            final DesktopSession desktopSession = desktopSessionMember.session();

            final RemoteDesktopConnection remoteDesktopConnection = desktopSessionMember.remoteDesktopConnection();
            InstanceActivityType controlActivityType = this.getControlActivityType(data);
            if (controlActivityType != null) {
                remoteDesktopConnection.setInstanceActivity(controlActivityType);
            }

            InstanceMemberRole role = desktopSessionMember.connectedUser().getRole();
            if (controlActivityType == null || role.equals(InstanceMemberRole.OWNER) || role.equals(InstanceMemberRole.SUPPORT) || (role.equals(InstanceMemberRole.USER) && !desktopSession.isLocked())) {
                this.writeData(remoteDesktopConnection.getConnectionThread(), data);
            }

            // Update last seen time of instance if more than one minute
            final Date lastInstanceUpdateTime = remoteDesktopConnection.getLastInstanceUpdateTime();
            final Date instanceInteractionTime = new Date();
            if (lastInstanceUpdateTime == null || (instanceInteractionTime.getTime() - lastInstanceUpdateTime.getTime() > 15 * 1000)) {
                remoteDesktopConnection.setLastInstanceUpdateTime(instanceInteractionTime);

                // Use virtual thread to update interaction times so that the websocket can return ASAP
                Uni.createFrom()
                    .voidItem()
                    .emitOn(Infrastructure.getDefaultWorkerPool())
                    .subscribe()
                    .with((voidItem) -> this.updateInstanceActivity(desktopSession.getInstanceId(), instanceInteractionTime, socketClient.clientId()));
            }
       });
    }

    private void updateInstanceActivity(final Long instanceId, final Date instanceInteractionTime, final String clientId) {
        final InstancePartial instance = this.instanceService.getPartialById(instanceId);
        if (instance == null) {
            return;
        }

        // Update instance
        instance.setLastSeenAt(instanceInteractionTime);
        instance.setLastInteractionAt(instanceInteractionTime);
        instanceService.updatePartial(instance);

        this.desktopSessionService.updateSessionMemberActivity(clientId, instanceInteractionTime);
    }

    protected abstract InstanceActivityType getControlActivityType(T data);

    protected abstract void writeData(ConnectionThread connectionThread, T data);
}
