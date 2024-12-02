package eu.ill.visa.vdi.display.sockets;

import eu.ill.visa.vdi.display.subscribers.RemoteDesktopConnectSubscriber;
import eu.ill.visa.vdi.display.subscribers.RemoteDesktopDisconnectSubscriber;
import eu.ill.visa.vdi.domain.models.SocketClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public abstract class RemoteDesktopSocket {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopSocket.class);

    private RemoteDesktopConnectSubscriber connectSubscriber;
    private RemoteDesktopDisconnectSubscriber disconnectSubscriber;

    private final ConcurrentHashMap<String, Object> sessionLocks = new ConcurrentHashMap<>();

    public void setConnectSubscriber(RemoteDesktopConnectSubscriber connectSubscriber) {
        this.connectSubscriber = connectSubscriber;
    }

    public void setDisconnectSubscriber(RemoteDesktopDisconnectSubscriber disconnectSubscriber) {
        this.disconnectSubscriber = disconnectSubscriber;
    }

    protected interface WorkerHandler {
        void onWork();
    }

    private void runOnWorker(final WorkerHandler handler) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .voidItem()
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(Void -> handler.onWork());
    }

    protected void connectOnWorker(final SocketClient socketClient, final WorkerHandler handler) {
        Object lock = sessionLocks.computeIfAbsent(socketClient.clientId(), id -> new Object());
        synchronized (lock) {
            this.runOnWorker(handler);
        }
    }

    protected void disconnectOnWorker(final SocketClient socketClient, final WorkerHandler handler) {
        Object lock = sessionLocks.computeIfAbsent(socketClient.clientId(), id -> new Object());
        synchronized (lock) {
            this.runOnWorker(handler);
            sessionLocks.remove(socketClient.clientId());
        }
    }

    protected void messageOnWorker(final SocketClient socketClient, final WorkerHandler handler) {
        Object lock = sessionLocks.get(socketClient.clientId());
        if (lock != null) {
            synchronized (lock) {
                this.runOnWorker(handler);
            }
        }
    }

    protected void onOpen(final SocketClient socketClient) {
        try {
            this.connectOnWorker(socketClient, () -> this.connectSubscriber.onConnect(socketClient, this::sendNop));

        } catch (Exception e) {
            logger.error("Failed to connect to RemoteDesktopSocket with protocol {}: {}", socketClient.protocol(), e.getMessage());
        }
    }

    protected void onClose(final SocketClient socketClient) {
        try {
            this.disconnectOnWorker(socketClient, () -> this.disconnectSubscriber.onDisconnect(socketClient));

        } catch (Exception e) {
            logger.error("Failed to disconnect from RemoteDesktopSocket with protocol {}: {}", socketClient.protocol(), e.getMessage());
        }
    }

    protected void onMessage(final SocketClient socketClient, WorkerHandler handler) {
        try {
            this.messageOnWorker(socketClient, handler);

        } catch (Exception e) {
            logger.error("Failed to handle message on RemoteDesktopSocket with protocol {}: {}", socketClient.protocol(), e.getMessage());
        }
    }

    protected abstract void sendNop(final SocketClient socketClient);

}
