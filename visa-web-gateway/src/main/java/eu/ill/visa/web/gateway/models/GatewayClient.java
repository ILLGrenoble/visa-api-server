package eu.ill.visa.web.gateway.models;

import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record GatewayClient(Session session, String token, String clientId) {

    private static final Logger logger = LoggerFactory.getLogger(GatewayClient.class);

    public void sendEvent(Object data) {
        this.session.getAsyncRemote().sendObject(data, result -> {
            if (result.getException() != null) {
                String dataString = data.toString();
                if (dataString.length() > 20) {
                    dataString = dataString.substring(0, 20) + "...";
                }
                logger.error("Unable to send message {} of type {} to client {}: {}", dataString, data.getClass().getName(), this.token, result.getException().getMessage());
            }
        });
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
