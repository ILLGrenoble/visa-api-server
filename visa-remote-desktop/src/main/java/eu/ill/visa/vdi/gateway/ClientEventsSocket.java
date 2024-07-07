package eu.ill.visa.vdi.gateway;

import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.ClientEventsGateway;
import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value="/ws/events/{token}",
    encoders = {ClientEventCarrierEncoderDecoder.class},
    decoders = {ClientEventCarrierEncoderDecoder.class})
@Singleton
public class ClientEventsSocket {

    private static final Logger logger = LoggerFactory.getLogger(ClientEventsSocket.class);

    private interface SocketClientWorkerConnectionHandler {
        void onWork(final SocketClient socketClient);
    }

    private interface SocketClientWorkerEventHandler {
        void onWork(final SocketClient socketClient, final ClientEventCarrier carrier);
    }

    private final ClientEventsGateway clientEventsGateway;

    @Inject
    public ClientEventsSocket(final ClientEventsGateway clientEventsGateway) {
        this.clientEventsGateway = clientEventsGateway;
    }

    @OnOpen
    private void onOpen(Session session, @PathParam("token") String token) {
        try {
            this.runOnWorker(new SocketClient(session, token), this.clientEventsGateway::onConnect);

        } catch (Exception e) {
            logger.error("Failed to connect to ClientEventsSocket: {}", e.getMessage());
        }
    }

    @OnClose
    private void onClose(Session session, @PathParam("token") String token) {
        try {
            this.runOnWorker(new SocketClient(session, token), this.clientEventsGateway::onDisconnect);

        } catch (Exception e) {
            logger.error("Failed to disconnect from ClientEventsSocket: {}", e.getMessage());
        }
    }

    @OnError
    private void onError(Session session, @PathParam("token") String token, Throwable throwable) {
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, ClientEventCarrier clientEventCarrier) {
        try {
            this.runOnWorker(new SocketClient(session, token), clientEventCarrier,  this.clientEventsGateway::onEvent);

        } catch (Exception e) {
            logger.error("Failed to disconnect from ClientEventsSocket: {}", e.getMessage());
        }
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, byte[] message) {
    }

    private <T> void runOnWorker(final SocketClient socketClient, final SocketClientWorkerConnectionHandler handler) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .item(socketClient)
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(handler::onWork);
    }

    private <T> void runOnWorker(final SocketClient socketClient, final ClientEventCarrier clientEventCarrier, final SocketClientWorkerEventHandler handler) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .item(socketClient)
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(client -> handler.onWork(client, clientEventCarrier));
    }
}
