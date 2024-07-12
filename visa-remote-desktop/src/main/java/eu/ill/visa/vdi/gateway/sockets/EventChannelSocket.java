package eu.ill.visa.vdi.gateway.sockets;

import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.ClientEventsGateway;
import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value="/ws/vdi/{token}/events",
    encoders = {ClientEventCarrierEncoderDecoder.class},
    decoders = {ClientEventCarrierEncoderDecoder.class})
@Singleton
public class EventChannelSocket extends AbstractSocket {

    private static final Logger logger = LoggerFactory.getLogger(EventChannelSocket.class);

    private final ClientEventsGateway clientEventsGateway;

    @Inject
    public EventChannelSocket(final ClientEventsGateway clientEventsGateway) {
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
        logger.error("Got event channel websocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, ClientEventCarrier clientEventCarrier) {
        try {
            this.runOnWorker(new SocketClient(session, token), clientEventCarrier, this.clientEventsGateway::onEvent);

        } catch (Exception e) {
            logger.error("Failed to handle message from ClientEventsSocket: {}", e.getMessage());
        }
    }
}
