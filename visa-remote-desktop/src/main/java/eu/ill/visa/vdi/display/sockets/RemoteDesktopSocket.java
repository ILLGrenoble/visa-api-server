package eu.ill.visa.vdi.display.sockets;

import eu.ill.visa.vdi.display.subscribers.RemoteDesktopConnectSubscriber;
import eu.ill.visa.vdi.display.subscribers.RemoteDesktopDisconnectSubscriber;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static eu.ill.visa.common.utils.exceptions.ExceptionFormatter.formatException;

public abstract class RemoteDesktopSocket {

    private enum WorkerEventType {
        CONNECT,
        DISCONNECT,
        MESSAGE,
    }

    private record WorkerEvent(SocketClient socketClient, Runnable worker, WorkerEventType type, Instant createdAt) implements Runnable {

        WorkerEvent(SocketClient socketClient, Runnable worker, WorkerEventType type) {
            this(socketClient, worker, type, Instant.now());
        }

        @Override
        public void run() {
            try {
                Instant eventStartTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
                double timeToStartEventMs = 0.001 * ChronoUnit.MICROS.between(this.createdAt, eventStartTime);

                worker.run();

                Instant eventEndTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
                double eventDurationMs = 0.001 * ChronoUnit.MICROS.between(eventStartTime, eventEndTime);
                double totalDurationMs = 0.001 * ChronoUnit.MICROS.between(this.createdAt, eventEndTime);

                if (this.type == WorkerEventType.CONNECT || this.type == WorkerEventType.DISCONNECT) {
                    String message = String.format("Remote Desktop Event (%s) for client %s: total=%.1fms, queue=%.1fms, exec=%.1fms", this.type, socketClient.clientId(), totalDurationMs, timeToStartEventMs, eventDurationMs);
                    if (totalDurationMs > 2000) {
                        logger.warn(message);

                    } else {
                        logger.info(message);
                    }

                } else if (this.type == WorkerEventType.MESSAGE && eventDurationMs > 1000) {
                    logger.warn("Remote Desktop Event (MESSAGE) for client {} slow to execute: {}ms", socketClient.clientId(), eventDurationMs);
                }

            } catch (Exception error) {
                logger.error("Remote Desktop Event ({}) for socket client {} failed with protocol {}: {}", type, socketClient.clientId(), socketClient.protocol(), formatException(error));
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopSocket.class);

    private RemoteDesktopConnectSubscriber connectSubscriber;
    private RemoteDesktopDisconnectSubscriber disconnectSubscriber;

    public void setConnectSubscriber(RemoteDesktopConnectSubscriber connectSubscriber) {
        this.connectSubscriber = connectSubscriber;
    }

    public void setDisconnectSubscriber(RemoteDesktopDisconnectSubscriber disconnectSubscriber) {
        this.disconnectSubscriber = disconnectSubscriber;
    }

    protected void onOpen(final SocketClient socketClient) {
        new WorkerEvent(socketClient, () -> this.connectSubscriber.onConnect(socketClient, this::sendNop), WorkerEventType.CONNECT).run();
    }

    protected void onClose(final SocketClient socketClient) {
        new WorkerEvent(socketClient, () -> this.disconnectSubscriber.onDisconnect(socketClient), WorkerEventType.DISCONNECT).run();
    }

    protected void onMessage(final SocketClient socketClient, Runnable handler) {
        new WorkerEvent(socketClient, handler, WorkerEventType.MESSAGE).run();
    }

    protected abstract void sendNop(final SocketClient socketClient);
}
