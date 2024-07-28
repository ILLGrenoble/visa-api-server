package eu.ill.visa.vdi.gateway.subscribers;

import eu.ill.visa.broker.gateway.ClientEventSubscriber;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;

public class AccessRevokedSubscriber implements ClientEventSubscriber<AccessRevokedEvent> {

    private final DesktopSessionService desktopSessionService;

    public AccessRevokedSubscriber(final DesktopSessionService desktopSessionService) {
        this.desktopSessionService = desktopSessionService;
    }

    @Override
    public void onEvent(final String clientId, final AccessRevokedEvent command) {
        this.desktopSessionService.findDesktopSessionMemberByClientId(clientId).ifPresent(desktopSessionMember -> {
            this.desktopSessionService.revokeUserAccess(desktopSessionMember, command.userId());
        });
    }
}
