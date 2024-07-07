package eu.ill.visa.vdi.domain.models;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class SocketClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String token;
    private SocketIOClient socketIOClient;
    private Session session;

    public SocketClient(final SocketIOClient socketIOClient, final String token) {
        this.socketIOClient = socketIOClient;
        this.token = token;
    }

    public SocketClient(final Session session, final String token) {
        this.session = session;
        this.token = token;
    }

    public String token() {
        return token;
    }

    public String getRequestParameter(final String key) {
        if (this.socketIOClient != null) {
            return this.socketIOClient.getHandshakeData().getSingleUrlParam(key);
        } else {
            return this.session.getRequestParameterMap().get(key).getFirst();
        }
    }

    public void sendEvent(String type) {
        this.sendEvent(type, null);
    }

    public <T> void sendEvent(String type, T event) {
        if (this.socketIOClient != null) {
            this.socketIOClient.sendEvent(type, event);

        } else {
            this.session.getAsyncRemote().sendObject(new ClientEventCarrier(type, event), result -> {
                if (result.getException() != null) {
                    logger.error("Unable to send message with type {} to client {}: {}", type, this.token, result.getException().getMessage());
                }
            });
        }
    }
    public void disconnect() {
        if (this.socketIOClient != null) {
            this.socketIOClient.disconnect();

        } else {
            try {
                this.session.close();
            } catch (IOException e) {
                logger.error("Failed to disconnect from WebSocket client {}: {}", this.token, e.getMessage());
            }
        }
    }

    public boolean isChannelOpen() {
        if (this.socketIOClient != null) {
            return this.socketIOClient.isChannelOpen();
        } else {
            return this.session.isOpen();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketClient that = (SocketClient) o;
        return Objects.equals(token, that.token) && Objects.equals(socketIOClient, that.socketIOClient) && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(token);
        result = 31 * result + Objects.hashCode(socketIOClient);
        result = 31 * result + Objects.hashCode(session);
        return result;
    }
}
