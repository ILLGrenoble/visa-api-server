package eu.ill.visa.vdi.display.sockets;

import eu.ill.visa.vdi.display.subscribers.RemoteDesktopConnectSubscriber;
import eu.ill.visa.vdi.display.subscribers.RemoteDesktopDisconnectSubscriber;
import eu.ill.visa.vdi.domain.models.SocketClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RemoteDesktopSocket {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopSocket.class);

    private RemoteDesktopConnectSubscriber connectSubscriber;
    private RemoteDesktopDisconnectSubscriber disconnectSubscriber;

    public void setConnectSubscriber(RemoteDesktopConnectSubscriber connectSubscriber) {
        this.connectSubscriber = connectSubscriber;
    }

    public void setDisconnectSubscriber(RemoteDesktopDisconnectSubscriber disconnectSubscriber) {
        this.disconnectSubscriber = disconnectSubscriber;
    }

    protected interface SocketClientWorkerConnectionHandler {
        void onWork(final SocketClient socketClient);
    }

    protected interface SocketClientWorkerEventHandler<T> {
        void onWork(final SocketClient socketClient, final T data);
    }

    protected void runOnWorker(final SocketClient socketClient, final SocketClientWorkerConnectionHandler handler) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .item(socketClient)
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(handler::onWork);
    }

    protected <T> void runOnWorker(final SocketClient socketClient, final T data, final SocketClientWorkerEventHandler<T> handler) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .item(socketClient)
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(client -> handler.onWork(client, data));
    }
    protected void onOpen(final SocketClient socketClient) {
        try {
            this.runOnWorker(socketClient, (client) -> this.connectSubscriber.onConnect(client, this::sendNop));

        } catch (Exception e) {
            logger.error("Failed to connect to RemoteDesktopSocket: {}", e.getMessage());
        }
    }

    protected void onClose(final SocketClient socketClient) {
        try {
            this.runOnWorker(socketClient, this.disconnectSubscriber::onDisconnect);

        } catch (Exception e) {
            logger.error("Failed to disconnect from RemoteDesktopSocket: {}", e.getMessage());
        }
    }

    protected abstract void sendNop(final SocketClient socketClient);

}
