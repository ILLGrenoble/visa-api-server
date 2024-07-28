package eu.ill.visa.vdi.display.sockets;

import eu.ill.visa.vdi.business.services.DesktopService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.display.subscribers.GuacamoleRemoteDesktopEventSubscriber;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value="/ws/vdi/{token}/{clientId}/guacamole", subprotocols = {"guacamole"})
@Singleton
public class GuacamoleRemoteDesktopSocket extends RemoteDesktopSocket {

    private static final Logger logger = LoggerFactory.getLogger(GuacamoleRemoteDesktopSocket.class);

    private GuacamoleRemoteDesktopEventSubscriber guacamoleRemoteDesktopEventSubscriber;

    public void setEventSubscriber(GuacamoleRemoteDesktopEventSubscriber guacamoleRemoteDesktopEventSubscriber) {
        this.guacamoleRemoteDesktopEventSubscriber = guacamoleRemoteDesktopEventSubscriber;
    }

    @OnOpen
    protected void onOpen(Session session, @PathParam("clientId") String clientId) {
        super.onOpen(new SocketClient(session, clientId, DesktopService.GUACAMOLE_PROTOCOL));
    }

    @OnClose
    protected void onClose(Session session, @PathParam("clientId") String clientId) {
        super.onClose(new SocketClient(session, clientId, DesktopService.GUACAMOLE_PROTOCOL));
    }

    protected void sendNop(final SocketClient socketClient) {
        socketClient.sendEvent("3.nop;");
    }

    @OnError
    private void onError(Session session, Throwable throwable) {
        logger.error("Got guacamole websocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("clientId") String clientId, String data) {
        try {
            this.runOnWorker(new SocketClient(session, clientId, DesktopService.GUACAMOLE_PROTOCOL), data,  this.guacamoleRemoteDesktopEventSubscriber::onEvent);

        } catch (Exception e) {
            logger.error("Failed to handle message from GuacamoleRemoteDesktopSocket: {}", e.getMessage());
        }
    }
}
