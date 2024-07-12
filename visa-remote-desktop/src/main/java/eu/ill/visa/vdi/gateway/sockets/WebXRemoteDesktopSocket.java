package eu.ill.visa.vdi.gateway.sockets;

import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.subscribers.WebXRemoteDesktopEventSubscriber;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value="/ws/vdi/{token}/webx", subprotocols = {"webx"})
@Singleton
public class WebXRemoteDesktopSocket extends RemoteDesktopSocket {

    private static final Logger logger = LoggerFactory.getLogger(WebXRemoteDesktopSocket.class);

    private WebXRemoteDesktopEventSubscriber webXRemoteDesktopEventSubscriber;

    public void setEventSubscriber(WebXRemoteDesktopEventSubscriber webXRemoteDesktopEventSubscriber) {
        this.webXRemoteDesktopEventSubscriber = webXRemoteDesktopEventSubscriber;
    }

    @OnOpen
    protected void onOpen(Session session, @PathParam("token") String token) {
        super.onOpen(session, token);
    }

    @OnClose
    protected void onClose(Session session, @PathParam("token") String token) {
        super.onClose(session, token);
    }

    @OnError
    private void onError(Session session, @PathParam("token") String token, Throwable throwable) {
        logger.error("Got webx websocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, byte[] data) {
        try {
            this.runOnWorker(new SocketClient(session, token), data, this.webXRemoteDesktopEventSubscriber::onEvent);

        } catch (Exception e) {
            logger.error("Failed to handle message from GuacamoleRemoteDesktopSocket: {}", e.getMessage());
        }
    }
}
