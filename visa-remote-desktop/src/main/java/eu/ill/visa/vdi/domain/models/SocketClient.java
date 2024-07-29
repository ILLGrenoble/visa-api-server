package eu.ill.visa.vdi.domain.models;

import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public record SocketClient(Session session, String clientId, String protocol) {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    public String getPathParameter(String parameterName) {
        return this.session.getPathParameters().get(parameterName);
    }

    public void sendEvent(Object data) {
        this.session.getAsyncRemote().sendObject(data, result -> {
            if (result.getException() != null) {
                String dataString = data.toString();
                if (dataString.length() > 20) {
                    dataString = dataString.substring(0, 20) + "...";
                }
                logger.error("Unable to send message {} of type {} to client {}: {}", dataString, data.getClass().getName(), this.clientId, result.getException().getMessage());
            }
        });
    }
    public void disconnect() {
        try {
            this.session.close();
        } catch (IOException e) {
            logger.error("Failed to disconnect from WebSocket client {}: {}", this.clientId, e.getMessage());
        }
    }

    public boolean isChannelOpen() {
        return this.session.isOpen();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketClient that = (SocketClient) o;
        return Objects.equals(session, that.session) && Objects.equals(clientId, that.clientId) && Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(session);
        result = 31 * result + Objects.hashCode(clientId);
        result = 31 * result + Objects.hashCode(protocol);
        return result;
    }
}
