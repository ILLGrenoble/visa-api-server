package eu.ill.visa.web.gateway.models;


import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import eu.ill.visa.broker.domain.models.EventChannelSubscription;
import eu.ill.visa.core.domain.IdleHandler;

public record GatewayTunnel(String clientId, GatewayClient gatewayClient, EventChannelSubscription subscription, IdleHandler idleHandler) {

    private static final int IDLE_TIMEOUT_SECONDS = 60;

    public GatewayTunnel(String clientId, GatewayClient gatewayClient, EventChannelSubscription subscription) {
        this(clientId, gatewayClient, subscription, new IdleHandler(IDLE_TIMEOUT_SECONDS));
    }

    public void sendEventToClient(String type) {
        this.sendEventToClient(type, null);
    }

    public void sendEventToClient(String type, Object data) {
        this.gatewayClient.sendEvent(new ClientEventCarrier(type, data));
    }
}
