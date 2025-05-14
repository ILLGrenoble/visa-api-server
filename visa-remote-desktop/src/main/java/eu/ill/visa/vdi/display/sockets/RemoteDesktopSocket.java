package eu.ill.visa.vdi.display.sockets;

import eu.ill.visa.vdi.display.subscribers.RemoteDesktopConnectSubscriber;
import eu.ill.visa.vdi.display.subscribers.RemoteDesktopDisconnectSubscriber;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class RemoteDesktopSocket {

    private enum WorkerEventType {
        CONNECT,
        DISCONNECT,
        MESSAGE,
    }

    private record WorkerEvent(SocketClient socketClient, Runnable worker, WorkerEventType type, Instant createdAt) {

        WorkerEvent(SocketClient socketClient, Runnable worker, WorkerEventType type) {
            this(socketClient, worker, type, Instant.now());
        }

        public void execute() {
            try {
                Instant eventStartTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
                double timeToStartEventMs = 0.001 * ChronoUnit.MICROS.between(this.createdAt, eventStartTime);

                if (this.type == WorkerEventType.CONNECT) {
                    logger.info("Remote Desktop Event (CONNECT) for client {} started in : {}ms", socketClient.clientId(), timeToStartEventMs);
                }
                if (timeToStartEventMs > 1000) {
                    logger.warn("Remote Desktop Event ({}) for client {} slow to start: {}ms", type, socketClient.clientId(), timeToStartEventMs);
                }

                worker.run();

                Instant eventEndTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
                double eventDurationMs = 0.001 * ChronoUnit.MICROS.between(eventStartTime, eventEndTime);

                if (this.type == WorkerEventType.CONNECT) {
                    logger.info("Remote Desktop Event (CONNECT) for client {} completed in : {}ms", socketClient.clientId(), eventDurationMs);

                } else if (this.type == WorkerEventType.MESSAGE && eventDurationMs > 1000) {
                    logger.warn("Remote Desktop Event (MESSAGE) for client {} slow to execute: {}ms", socketClient.clientId(), timeToStartEventMs);
                }


            } catch (Exception error) {
                logger.error("Remote Desktop Event ({}) failed with protocol {}: {}", type, socketClient.protocol(), error.getMessage());
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopSocket.class);

    private RemoteDesktopConnectSubscriber connectSubscriber;
    private RemoteDesktopDisconnectSubscriber disconnectSubscriber;

    private final ConcurrentHashMap<String, LinkedList<WorkerEvent>> sessionQueues = new ConcurrentHashMap<>();

    private final Executor desktopEventExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vdi-vt-", 0).factory());;

    public RemoteDesktopSocket() {
    }

    public void setConnectSubscriber(RemoteDesktopConnectSubscriber connectSubscriber) {
        this.connectSubscriber = connectSubscriber;
    }

    public void setDisconnectSubscriber(RemoteDesktopDisconnectSubscriber disconnectSubscriber) {
        this.disconnectSubscriber = disconnectSubscriber;
    }

    private void handleEvent(final WorkerEvent workerEvent) {
        final LinkedList<WorkerEvent> queue = sessionQueues.get(workerEvent.socketClient().clientId());
        // If queue doesn't exist then send the event immediately
        if (queue == null) {
            this.asyncRunEvent(workerEvent);

        } else {
            // lock the queue
            synchronized (queue) {
                // Check if lock still valid (will be removed once the connect event has finished and the queue is empty)
                if (sessionQueues.containsKey(workerEvent.socketClient().clientId())) {
                    // Add event to the queue
                    queue.addLast(workerEvent);

                    // Run event immediately if it is the only one on the queue
                    if (queue.size() == 1) {
                        this.asyncRunQueuedEvent(workerEvent);
                    }

                } else {
                    this.asyncRunEvent(workerEvent);
                }
            }
        }

    }

    private void onWorkerEventTerminated(final WorkerEvent event) {
        final LinkedList<WorkerEvent> queue = sessionQueues.get(event.socketClient().clientId());
        if (queue == null) {
            return;
        }

        synchronized (queue) {
            queue.removeFirst();

            if (queue.isEmpty()) {
                sessionQueues.remove(event.socketClient().clientId());

            } else {
                final WorkerEvent nextWorkerEvent = queue.getFirst();
                this.asyncRunQueuedEvent(nextWorkerEvent);
            }
        }
    }

    private void asyncRunEvent(final WorkerEvent event) {
        // Execute handler on virtual thread to allow JTA transactions
        CompletableFuture.runAsync(event::execute, desktopEventExecutor);
    }

    private void asyncRunQueuedEvent(final WorkerEvent event) {
        // Execute handler on virtual thread to allow JTA transactions
        CompletableFuture.runAsync(() -> {
            event.execute();
            this.onWorkerEventTerminated(event);

        }, desktopEventExecutor);
    }

    protected void onOpen(final SocketClient socketClient) {
        sessionQueues.put(socketClient.clientId(), new LinkedList<>());
        this.handleEvent(new WorkerEvent(socketClient, () -> this.connectSubscriber.onConnect(socketClient, this::sendNop), WorkerEventType.CONNECT));
    }

    protected void onClose(final SocketClient socketClient) {
        this.handleEvent(new WorkerEvent(socketClient, () -> this.disconnectSubscriber.onDisconnect(socketClient), WorkerEventType.DISCONNECT));
    }

    protected void onMessage(final SocketClient socketClient, Runnable handler) {
        this.handleEvent(new WorkerEvent(socketClient, handler, WorkerEventType.MESSAGE));
    }

    protected abstract void sendNop(final SocketClient socketClient);

}
