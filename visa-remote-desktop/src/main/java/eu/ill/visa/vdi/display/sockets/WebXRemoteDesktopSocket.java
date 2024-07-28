package eu.ill.visa.vdi.display.sockets;

import eu.ill.visa.vdi.business.services.DesktopService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.display.subscribers.WebXRemoteDesktopEventSubscriber;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value="/ws/vdi/{token}/{clientId}/webx", subprotocols = {"webx"})
@Singleton
public class WebXRemoteDesktopSocket extends RemoteDesktopSocket {

    private static final Logger logger = LoggerFactory.getLogger(WebXRemoteDesktopSocket.class);

    private WebXRemoteDesktopEventSubscriber webXRemoteDesktopEventSubscriber;

    public void setEventSubscriber(WebXRemoteDesktopEventSubscriber webXRemoteDesktopEventSubscriber) {
        this.webXRemoteDesktopEventSubscriber = webXRemoteDesktopEventSubscriber;
    }

    @OnOpen
    protected void onOpen(Session session, @PathParam("clientId") String clientId) {
        super.onOpen(new SocketClient(session, clientId, DesktopService.WEBX_PROTOCOL));
    }

    @OnClose
    protected void onClose(Session session, @PathParam("clientId") String clientId) {
        super.onClose(new SocketClient(session, clientId, DesktopService.WEBX_PROTOCOL));
    }

    protected void sendNop(final SocketClient socketClient) {
    }

    @OnError
    private void onError(Session session, Throwable throwable) {
        logger.error("Got webx websocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("clientId") String clientId, byte[] data) {
        try {
            this.runOnWorker(new SocketClient(session, clientId, DesktopService.WEBX_PROTOCOL), data, this.webXRemoteDesktopEventSubscriber::onEvent);

        } catch (Exception e) {
            logger.error("Failed to handle message from GuacamoleRemoteDesktopSocket: {}", e.getMessage());
        }
    }
}
