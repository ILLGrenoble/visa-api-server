package eu.ill.visa.vdi.domain.models;

import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class SocketClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String token;
    private final Session session;

    public SocketClient(final Session session, final String token) {
        this.session = session;
        this.token = token;
    }

    public String token() {
        return token;
    }

    public String getRequestParameter(final String key) {
        return this.session.getRequestParameterMap().get(key).getFirst();
    }

    public void sendEvent(Object data) {
        this.session.getAsyncRemote().sendObject(data, result -> {
            if (result.getException() != null) {
                String dataString = data.toString();
                if (dataString.length() > 20) {
                    dataString = dataString.substring(0, 20) + "...";
                }
                logger.error("Unable to send message {} to client {}: {}", dataString, this.token, result.getException().getMessage());
            }
        });
    }
    public void disconnect() {
        try {
            this.session.close();
        } catch (IOException e) {
            logger.error("Failed to disconnect from WebSocket client {}: {}", this.token, e.getMessage());
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
        return Objects.equals(token, that.token) && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(token);
        result = 31 * result + Objects.hashCode(session);
        return result;
    }
}
