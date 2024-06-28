package eu.ill.visa.vdi.gateway.listeners;

import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.ClientEventSubscriber;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;

public class ClientAccessRevokedListener implements ClientEventSubscriber<AccessRevokedEvent> {

    private final DesktopSessionService desktopSessionService;

    public ClientAccessRevokedListener(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    @Override
    public void onEvent(final SocketClient client, final AccessRevokedEvent command) {
        this.desktopSessionService.findDesktopSessionMember(client).ifPresent(desktopSessionMember -> {
            this.desktopSessionService.revokeUserAccess(desktopSessionMember, command.userId());
        });
    }
}
