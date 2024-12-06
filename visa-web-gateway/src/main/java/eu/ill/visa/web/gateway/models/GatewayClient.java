package eu.ill.visa.web.gateway.models;

import eu.ill.visa.core.domain.IdleHandler;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record GatewayClient(Session session, String token, String clientId, IdleHandler idleHandler) {

    private static final int IDLE_TIMEOUT_SECONDS = 60;

    private static final Logger logger = LoggerFactory.getLogger(GatewayClient.class);

    public GatewayClient(Session session, String token, String clientId) {
        this(session, token, clientId, new IdleHandler(IDLE_TIMEOUT_SECONDS));
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GatewayClient that = (GatewayClient) o;
        return Objects.equals(token, that.token) && Objects.equals(session, that.session) && Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(session);
        result = 31 * result + Objects.hashCode(token);
        result = 31 * result + Objects.hashCode(clientId);
        return result;
    }
}
