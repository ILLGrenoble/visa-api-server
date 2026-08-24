package eu.ill.visa.web.gateway.models;

import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record GatewayClient(Session session, String token, String clientId) {

    private static final Logger logger = LoggerFactory.getLogger(GatewayClient.class);

    public void sendEvent(Object data) {
        if (session.isOpen()) {
            this.session.getAsyncRemote().sendObject(data, result -> {
                if (result.getException() != null) {
                    String dataString = data.toString();
                    if (dataString.length() > 20) {
                        dataString = dataString.substring(0, 20) + "...";
                    }
                    logger.warn("Unable to send message {} of type {} to gateway client {}: {}", dataString, data.getClass().getName(), this.token, result.getException().getMessage());
                }
            });
        }
    }
}
