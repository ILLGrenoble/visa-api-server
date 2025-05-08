package eu.ill.visa.vdi.domain.models;

import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SocketClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final Session session;
    private final String clientId;
    private final String protocol;
    private boolean disconnected = false;

    public SocketClient(Session session, String clientId, String protocol) {
        this.session = session;
        this.clientId = clientId;
        this.protocol = protocol;
    }

    public Session session() {
        return session;
    }

    public String clientId() {
        return clientId;
    }

    public String protocol() {
        return protocol;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    /**
     * Sets the socket client state to disconnected, before potentially the end of the disconnection method
     */
    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public String getPathParameter(String parameterName) {
        return this.session.getPathParameters().get(parameterName);
    }

    public Optional<String> getStringRequestParameter(String parameterName) {
        List<String> paramValues = this.session.getRequestParameterMap().get(parameterName);
        if (paramValues != null && !paramValues.isEmpty()) {
            return Optional.of(paramValues.getFirst());
        }
        return Optional.empty();
    }

    public void sendEvent(Object data) {
        if (!this.disconnected && session.isOpen()) {
            this.session.getAsyncRemote().sendObject(data, result -> {
                if (result.getException() != null) {
                    String dataString = data.toString();
                    if (dataString.length() > 20) {
                        dataString = dataString.substring(0, 20) + "...";
                    }
                    logger.warn("Unable to send message {} of type {} to client {}: {}", dataString, data.getClass().getName(), this.clientId, result.getException().getMessage());
                }
            });
        }
    }

    public void disconnect() {
        try {
            this.disconnected = true;
            this.session.close();
        } catch (IOException e) {
            logger.warn("Failed to disconnect from WebSocket client {}: {}", this.clientId, e.getMessage());
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
