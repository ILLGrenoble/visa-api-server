package eu.ill.visa.vdi.gateway.sockets;

import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.SocketConnectSubscriber;
import eu.ill.visa.vdi.gateway.dispatcher.SocketDisconnectSubscriber;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RemoteDesktopSocket extends AbstractSocket {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopSocket.class);

    private SocketConnectSubscriber socketConnectSubscriber;
    private SocketDisconnectSubscriber socketDisconnectSubscriber;

    public void setConnectSubscriber(SocketConnectSubscriber socketConnectSubscriber) {
        this.socketConnectSubscriber = socketConnectSubscriber;
    }

    public void setDisconnectSubscriber(SocketDisconnectSubscriber socketDisconnectSubscriber) {
        this.socketDisconnectSubscriber = socketDisconnectSubscriber;
    }

    protected void onOpen(Session session, String token) {
        try {
            this.runOnWorker(new SocketClient(session, token), (client) -> this.socketConnectSubscriber.onConnect(client, this::sendNop));

        } catch (Exception e) {
            logger.error("Failed to connect to RemoteDesktopSocket: {}", e.getMessage());
        }
    }

    protected void onClose(Session session, String token) {
        try {
            this.runOnWorker(new SocketClient(session, token), this.socketDisconnectSubscriber::onDisconnect);

        } catch (Exception e) {
            logger.error("Failed to disconnect from RemoteDesktopSocket: {}", e.getMessage());
        }
    }

    protected abstract void sendNop(final SocketClient socketClient);

}
