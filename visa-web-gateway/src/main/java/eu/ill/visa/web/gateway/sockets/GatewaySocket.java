package eu.ill.visa.web.gateway.sockets;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import eu.ill.visa.broker.domain.models.EventChannelSubscription;
import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.business.services.ClientAuthenticationTokenService;
import eu.ill.visa.core.entity.ClientAuthenticationToken;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.gateway.models.GatewayClient;
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

@ServerEndpoint(value="/ws/{token}/{clientId}/gateway",
    encoders = {ClientEventCarrierEncoderDecoder.class},
    decoders = {ClientEventCarrierEncoderDecoder.class})
@Singleton
public class GatewaySocket {

    private static final Logger logger = LoggerFactory.getLogger(GatewaySocket.class);

    private final EventDispatcher eventDispatcher;
    private final ClientAuthenticationTokenService authenticator;

    private final List<EventChannelSubscription> subscriptions = new ArrayList<>();

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

    private void handleOpen(Session session, String token, String clientId) {

        try {
            final ClientAuthenticationToken clientAuthenticationToken = authenticator.authenticate(token, clientId);
            final User user = clientAuthenticationToken.getUser();

            logger.info("Gateway websocket connected for user {} (id = {}) with client Id {}", user.getFullName(), user.getId(), clientId);

            final GatewayClient gatewayClient = new GatewayClient(session, token, clientId);

            final EventChannelSubscription subscription = this.eventDispatcher.subscribe(clientId, user.getId(), user.getRoles().stream().map(Role::getName).toList(), gatewayClient::sendEvent);
            this.subscriptions.add(subscription);

        } catch (InvalidTokenException e) {
            logger.error("GatewaySocket failed to connect: {}", e.getMessage());
        }
    }

    @OnClose
    private void onClose(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId) {
        final EventChannelSubscription subscription = this.subscriptions.stream()
            .filter(aSubscription -> aSubscription.clientId().equals(clientId))
            .findAny()
            .orElse(null);

        if (subscription != null) {
            this.subscriptions.remove(subscription);
            this.eventDispatcher.unsubscribe(subscription);

            logger.info("Gateway websocket closed for user with Id {} with client Id {}", subscription.userId(), subscription.clientId());
        }
    }

    @OnMessage
    private void onMessage(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId, ClientEventCarrier clientEventCarrier) {
        try {
            // Execute handler on worker thread to allow JTA transactions
            Uni.createFrom()
                .voidItem()
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with((voidItem) -> this.eventDispatcher.forwardEventFromClient(clientId, clientEventCarrier));

        } catch (Exception e) {
            logger.error("Failed to handle message on GatewaySocket: {}", e.getMessage());
        }
    }

    @OnError
    private void onError(Session session, @PathParam("token") String token, @PathParam("clientId") String clientId, Throwable throwable) {
        logger.error("Got GatewaySocket error for session {}: {}", session.getId(), throwable.getMessage());
    }

}
