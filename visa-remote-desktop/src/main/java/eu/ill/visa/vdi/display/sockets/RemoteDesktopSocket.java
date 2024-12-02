package eu.ill.visa.vdi.display.sockets;

import eu.ill.visa.vdi.display.subscribers.RemoteDesktopConnectSubscriber;
import eu.ill.visa.vdi.display.subscribers.RemoteDesktopDisconnectSubscriber;
import eu.ill.visa.vdi.domain.models.SocketClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RemoteDesktopSocket {

    protected interface WorkerHandler {
        void doWork();
    }

    private enum WorkerEventType {
        CONNECT,
        DISCONNECT,
        MESSAGE,
    }

    private record WorkerEvent(SocketClient socketClient, WorkerHandler worker, WorkerEventType type) { }

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopSocket.class);

    private RemoteDesktopConnectSubscriber connectSubscriber;
    private RemoteDesktopDisconnectSubscriber disconnectSubscriber;

    private final ConcurrentHashMap<String, Object> sessionLocks = new ConcurrentHashMap<>();
    private final Map<String, LinkedList<WorkerEvent>> sessionEventQueues = new HashMap<>();


    public void setConnectSubscriber(RemoteDesktopConnectSubscriber connectSubscriber) {
        this.connectSubscriber = connectSubscriber;
    }

    public void setDisconnectSubscriber(RemoteDesktopDisconnectSubscriber disconnectSubscriber) {
        this.disconnectSubscriber = disconnectSubscriber;
    }

    private void queueEvent(final WorkerEvent workerEvent) {
        Object lock = sessionLocks.computeIfAbsent(workerEvent.socketClient().clientId(), id -> new Object());
        synchronized (lock) {
            // Check if lock still valid
            if (sessionLocks.containsKey(workerEvent.socketClient().clientId())) {
                final LinkedList<WorkerEvent> sessionEventQueue = sessionEventQueues.computeIfAbsent(workerEvent.socketClient.clientId(), id -> new LinkedList<>());
                sessionEventQueue.addLast(workerEvent);

                // Run event immediately if it is the only one on the queue
                if (sessionEventQueue.size() == 1) {
                    this.runEvent(workerEvent);
                }
            }
        }
    }

    private void onWorkerEventTerminated(final WorkerEvent event) {
        Object lock = sessionLocks.computeIfAbsent(event.socketClient().clientId(), id -> new Object());
        synchronized (lock) {
            final LinkedList<WorkerEvent> sessionEventQueue = sessionEventQueues.get(event.socketClient().clientId());
            sessionEventQueue.removeFirst();

            if (event.type().equals(WorkerEventType.DISCONNECT)) {
                sessionLocks.remove(event.socketClient().clientId());
                sessionEventQueues.remove(event.socketClient().clientId());

            } else {
                if (!sessionEventQueue.isEmpty()) {
                    final WorkerEvent nextWorkerEvent = sessionEventQueue.getFirst();
                    this.runEvent(nextWorkerEvent);
                }
            }
        }
    }

    private void runEvent(final WorkerEvent event) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .voidItem()
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(Void -> {
                try {
                    event.worker().doWork();

                } catch (Exception error) {
                    logger.error("Remote Desktop Event ({}) failed with protocol {}: {}", event.type(), event.socketClient().protocol(), error.getMessage());
                }

                this.onWorkerEventTerminated(event);
            });
    }

    protected void onOpen(final SocketClient socketClient) {
        this.queueEvent(new WorkerEvent(socketClient, () -> this.connectSubscriber.onConnect(socketClient, this::sendNop), WorkerEventType.CONNECT));
    }

    protected void onClose(final SocketClient socketClient) {
        this.queueEvent(new WorkerEvent(socketClient, () -> this.disconnectSubscriber.onDisconnect(socketClient), WorkerEventType.DISCONNECT));
    }

    protected void onMessage(final SocketClient socketClient, WorkerHandler handler) {
        this.queueEvent(new WorkerEvent(socketClient, handler, WorkerEventType.MESSAGE));
    }

    protected abstract void sendNop(final SocketClient socketClient);

}
