package eu.ill.visa.web.gateway.sockets;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.business.services.ClientAuthenticationTokenService;
import eu.ill.visa.core.entity.ClientAuthenticationToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.gateway.models.GatewayClient;
import eu.ill.visa.web.gateway.models.GatewayTunnel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ServerEndpoint(value="/ws/{token}/{clientId}/gateway",
    encoders = {ClientEventCarrierEncoderDecoder.class},
    decoders = {ClientEventCarrierEncoderDecoder.class})
@Singleton
public class GatewaySocket {

    private enum GatewayEventType {
        CONNECT,
        DISCONNECT,
        MESSAGE,
    }

    private record GatewayEvent(Runnable runnable, GatewayEventType type) {
        public void execute() {
            try {
                runnable.run();

            } catch (Exception error) {
                logger.error("Gateway Event ({}) failed: {}", type, error.getMessage());
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(GatewaySocket.class);

    private final EventDispatcher eventDispatcher;
    private final ClientAuthenticationTokenService authenticator;

    private final Map<String, GatewayTunnel> gatewayTunnels = new HashMap<>();

    private final Executor gatewayEventExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("gateway-vt-", 0).factory());

    @Inject
    public GatewaySocket(final EventDispatcher eventDispatcher,
                         final ClientAuthenticationTokenService authenticator) {
        this.eventDispatcher = eventDispatcher;
        this.authenticator = authenticator;
    }

    @OnOpen
    private void onOpen(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId) {
        // Execute handler on virtual thread to allow JTA transactions
        this.asyncRunEvent(new GatewayEvent(() -> this.handleOpen(session, token, clientId), GatewayEventType.CONNECT));
    }

    @OnClose
    private void onClose(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId) {
        this.handleClose(clientId);
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId, ClientEventCarrier clientEventCarrier) {
        this.asyncRunEvent(new GatewayEvent(() -> this.handleMessage(clientId, clientEventCarrier), GatewayEventType.MESSAGE));
    }

    @OnError
    private void onError(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId, Throwable throwable) {
        logger.error("Got GatewaySocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

    private void asyncRunEvent(final GatewayEvent gatewayEvent) {
        CompletableFuture.runAsync(gatewayEvent::execute, gatewayEventExecutor);
    }

    private void handleOpen(Session session, String token, String clientId) {
        try {
            final ClientAuthenticationToken clientAuthenticationToken = authenticator.authenticate(token, clientId);
            final User user = clientAuthenticationToken.getUser();
            final String userName = user.getFullName();
            final String userId = user.getId();

            logger.info("Gateway websocket connected for user {} (id = {}) with client Id {}", user.getFullName(), user.getId(), clientId);

            final GatewayClient gatewayClient = new GatewayClient(session, token, clientId);
            final GatewayTunnel gatewayTunnel = this.createGatewayTunnel(user, gatewayClient, () -> this.handleIdle(clientId, userName, userId));

        } catch (InvalidTokenException e) {
            logger.error("GatewaySocket failed to connect: {}", e.getMessage());
        }
    }

    private void handleClose(String clientId) {
        this.deleteGatewayTunnel(clientId).ifPresent(GatewayTunnel::onClose);
    }

    private void handleMessage(String clientId, ClientEventCarrier clientEventCarrier) {
        // Reset the idle handler
        this.getGatewayTunnel(clientId).ifPresent(gatewayTunnel -> {
            gatewayTunnel.onEvent(clientEventCarrier);
        });
    }

    private void handleIdle(String clientId, final String userName, final String userId) {
        logger.warn("Idle timeout for gateway websocket for user {} (id = {}) with client Id {}: disconnecting", userName, userId, clientId);

        this.getGatewayTunnel(clientId).ifPresent(gatewayTunnel -> {
            try {
                // Ensure websocket is fully closed (sends event to client so that it'll attempt to reconnect
                gatewayTunnel.gatewayClient().session().close();
            } catch (Exception ignored) {
            }
        });

        this.handleClose(clientId);
    }

    private synchronized GatewayTunnel createGatewayTunnel(final User user, GatewayClient gatewayClient, Runnable idleHandlerCallback) {
        final GatewayTunnel gatewayTunnel = new GatewayTunnel(user, gatewayClient, this.eventDispatcher, idleHandlerCallback);
        this.gatewayTunnels.put(gatewayClient.clientId(), gatewayTunnel);
        return gatewayTunnel;
    }

    private synchronized Optional<GatewayTunnel> getGatewayTunnel(String clientId) {
        return Optional.ofNullable(this.gatewayTunnels.get(clientId));
    }

    private synchronized Optional<GatewayTunnel> deleteGatewayTunnel(String clientId) {
        return Optional.ofNullable(this.gatewayTunnels.remove(clientId));
    }

}
