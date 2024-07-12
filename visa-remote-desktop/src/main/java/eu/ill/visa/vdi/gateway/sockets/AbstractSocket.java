package eu.ill.visa.vdi.gateway.sockets;

import eu.ill.visa.vdi.domain.models.SocketClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

public class AbstractSocket {

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
}
