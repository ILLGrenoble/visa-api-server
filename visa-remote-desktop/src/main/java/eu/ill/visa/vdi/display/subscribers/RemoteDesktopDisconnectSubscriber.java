package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteDesktopDisconnectSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopDisconnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;

    public RemoteDesktopDisconnectSubscriber(final DesktopSessionService desktopSessionService,
                                             final DesktopAccessService desktopAccessService) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
    }

    public void onDisconnect(final SocketClient socketClient) {
        this.desktopSessionService.findDesktopSessionMemberByClientId(socketClient.clientId())
            .ifPresentOrElse(desktopSessionMember -> {
                logger.info("Remote Desktop Connection disconnected by client {}: disconnecting Session Member: {}", socketClient.clientId(), desktopSessionMember);
                this.desktopSessionService.onDesktopMemberDisconnect(desktopSessionMember);
            }, () -> {
                this.desktopAccessService.cancelAccessRequest(socketClient);
        });
    }
}

