package eu.ill.visa.web.gateway.models;


import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import eu.ill.visa.broker.domain.models.EventChannelSubscription;
import eu.ill.visa.core.domain.IdleHandler;
import eu.ill.visa.core.domain.Timer.ReusableTimer;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public record GatewayTunnel(GatewayClient gatewayClient, EventDispatcher eventDispatcher, EventChannelSubscription subscription, IdleHandler idleHandler, ReusableTimer pingTimer) {

    private static final Logger logger = LoggerFactory.getLogger(GatewayTunnel.class);

    private static final int IDLE_TIMEOUT_SECONDS = 60;

    public GatewayTunnel(User user, GatewayClient gatewayClient, EventDispatcher eventDispatcher, Runnable idleHandlerCallback) {
        this(gatewayClient,
             eventDispatcher,
             eventDispatcher.subscribe(gatewayClient.clientId(), user.getId(), user.getRoles().stream().map(Role::getName).toList(), gatewayClient::sendEvent),
             new IdleHandler(IDLE_TIMEOUT_SECONDS),
             new ReusableTimer(() -> gatewayClient.sendEvent(new ClientEventCarrier("ping", null)), 5, TimeUnit.SECONDS));

        // Activate the idle handler and ping timer
        this.idleHandler.start(idleHandlerCallback);
        this.pingTimer.start();
    }

    public String clientId() {
        return this.gatewayClient().clientId();
    }

    public void onClose() {
        // Stop the idle handler and ping timer
        this.idleHandler.stop();
        this.pingTimer.stop();

        // Remove event subscription
        this.eventDispatcher.unsubscribe(this.subscription);

        logger.info("Gateway websocket closed for user with Id {} with client Id {}", this.subscription.userId(), this.clientId());
    }

    public void onEvent(ClientEventCarrier clientEventCarrier) {
        // Reset idle handler
        this.idleHandler.reset();

        // Handle event
        if (clientEventCarrier.type().equals("pong")) {
            // Handle simple ping-pong keep-alive response
            this.pingTimer.start();

        } else {
            this.eventDispatcher.forwardEventFromClient(this.clientId(), clientEventCarrier);
        }
    }
}
