package eu.ill.visa.vdi.gateway.subscribers;

import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.SocketEventSubscriber;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;

public class EventChannelAccessRevokedSubscriber implements SocketEventSubscriber<AccessRevokedEvent> {

    private final DesktopSessionService desktopSessionService;

    public EventChannelAccessRevokedSubscriber(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    @Override
    public void onEvent(final SocketClient client, final AccessRevokedEvent command) {
        this.desktopSessionService.findDesktopSessionMemberByToken(client.token()).ifPresent(desktopSessionMember -> {
            this.desktopSessionService.revokeUserAccess(desktopSessionMember, command.userId());
        });
    }
}
