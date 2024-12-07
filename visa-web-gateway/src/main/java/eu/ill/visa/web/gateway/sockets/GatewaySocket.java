package eu.ill.visa.web.gateway.sockets;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.business.services.ClientAuthenticationTokenService;
import eu.ill.visa.core.entity.ClientAuthenticationToken;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.gateway.models.GatewayClient;
import eu.ill.visa.web.gateway.models.GatewayTunnel;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ServerEndpoint(value="/ws/{token}/{clientId}/gateway",
    encoders = {ClientEventCarrierEncoderDecoder.class},
    decoders = {ClientEventCarrierEncoderDecoder.class})
@Singleton
public class GatewaySocket {

    private static final Logger logger = LoggerFactory.getLogger(GatewaySocket.class);

    private final EventDispatcher eventDispatcher;
    private final ClientAuthenticationTokenService authenticator;

    private final List<GatewayTunnel> gatewayTunnels = new ArrayList<>();

    @Inject
    public GatewaySocket(final EventDispatcher eventDispatcher,
                         final ClientAuthenticationTokenService authenticator) {
        this.eventDispatcher = eventDispatcher;
        this.authenticator = authenticator;
    }

    @OnOpen
    private void onOpen(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId) {
        try {
            // Execute handler on worker thread to allow JTA transactions
            Uni.createFrom()
                .voidItem()
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with((voidItem) -> this.handleOpen(session, token, clientId));

        } catch (Exception e) {
            logger.error("Failed to connect to GatewaySocket: {}", e.getMessage());
        }
    }

    @OnClose
    private void onClose(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId) {
        this.handleClose(clientId);
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId, ClientEventCarrier clientEventCarrier) {
        try {
            // Execute handler on worker thread to allow JTA transactions
            Uni.createFrom()
                .voidItem()
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with((voidItem) -> this.handleMessage(clientId, clientEventCarrier));

        } catch (Exception e) {
            logger.error("Failed to handle message on GatewaySocket: {}", e.getMessage());
        }
    }

    @OnError
    private void onError(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId, Throwable throwable) {
        logger.error("Got GatewaySocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

    private void handleOpen(Session session, String token, String clientId) {
        try {
            final ClientAuthenticationToken clientAuthenticationToken = authenticator.authenticate(token, clientId);
            final User user = clientAuthenticationToken.getUser();

            logger.info("Gateway websocket connected for user {} (id = {}) with client Id {}", user.getFullName(), user.getId(), clientId);

            final GatewayClient gatewayClient = new GatewayClient(session, token, clientId);
            final GatewayTunnel gatewayTunnel = this.createGatewayTunnel(user, gatewayClient, () -> this.handleIdle(clientId));

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

    private void handleIdle(String clientId) {
        logger.warn("Idle timeout for gateway websocket {}: disconnecting", clientId);

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
        this.gatewayTunnels.add(gatewayTunnel);
        return gatewayTunnel;
    }

    private synchronized Optional<GatewayTunnel> getGatewayTunnel(String clientId) {
        return this.gatewayTunnels.stream().filter(gatewayTunnel -> gatewayTunnel.clientId().equals(clientId)).findAny();
    }

    private synchronized Optional<GatewayTunnel> deleteGatewayTunnel(String clientId) {
        final GatewayTunnel gatewayTunnel = this.getGatewayTunnel(clientId).orElse(null);
        if (gatewayTunnel != null) {
            this.gatewayTunnels.remove(gatewayTunnel);
            return Optional.of(gatewayTunnel);

        } else {
            return Optional.empty();
        }
    }

}
