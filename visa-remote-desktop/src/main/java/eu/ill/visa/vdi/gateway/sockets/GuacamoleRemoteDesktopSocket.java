package eu.ill.visa.vdi.gateway.sockets;

import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.subscribers.display.GuacamoleRemoteDesktopEventSubscriber;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value="/ws/vdi/{token}/guacamole", subprotocols = {"guacamole"})
@Singleton
public class GuacamoleRemoteDesktopSocket extends RemoteDesktopSocket {

    private static final Logger logger = LoggerFactory.getLogger(GuacamoleRemoteDesktopSocket.class);

    private GuacamoleRemoteDesktopEventSubscriber guacamoleRemoteDesktopEventSubscriber;

    public void setEventSubscriber(GuacamoleRemoteDesktopEventSubscriber guacamoleRemoteDesktopEventSubscriber) {
        this.guacamoleRemoteDesktopEventSubscriber = guacamoleRemoteDesktopEventSubscriber;
    }

    @OnOpen
    protected void onOpen(Session session, @PathParam("token") String token) {
        super.onOpen(session, token);
    }

    @OnClose
    protected void onClose(Session session, @PathParam("token") String token) {
        super.onClose(session, token);
    }

    protected void sendNop(final SocketClient socketClient) {
        socketClient.sendEvent("3.nop;");
    }

    @OnError
    private void onError(Session session, @PathParam("token") String token, Throwable throwable) {
        logger.error("Got guacamole websocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, String data) {
        try {
            this.runOnWorker(new SocketClient(session, token), data,  this.guacamoleRemoteDesktopEventSubscriber::onEvent);

        } catch (Exception e) {
            logger.error("Failed to handle message from GuacamoleRemoteDesktopSocket: {}", e.getMessage());
        }
    }
}
