package eu.ill.visa.vdi.gateway.subscribers.display;

import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.SocketDisconnectSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteDesktopDisconnectSubscriber implements SocketDisconnectSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopDisconnectSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final DesktopAccessService desktopAccessService;

    public RemoteDesktopDisconnectSubscriber(final DesktopSessionService desktopSessionService,
                                             final DesktopAccessService desktopAccessService) {
        this.desktopSessionService = desktopSessionService;
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onDisconnect(final SocketClient socketClient) {
        this.desktopSessionService.findDesktopSessionMemberByToken(socketClient.token())
            .ifPresentOrElse(desktopSessionMember -> {
                logger.info("Remote Desktop Connection disconnected by client {}: disconnecting Session Member: {}", socketClient.token(), desktopSessionMember);
                this.desktopSessionService.onDesktopMemberDisconnect(desktopSessionMember);
            }, () -> {
                this.desktopAccessService.cancelAccessRequest(socketClient);
        });
    }
}

