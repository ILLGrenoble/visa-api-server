package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.vdi.gateway.listeners.ClientConnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

public class DesktopSessionMember {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnectListener.class);

    private final String id;
    private final ConnectedUser connectedUser;
    private final SessionEventConnection eventConnection;
    private final RemoteDesktopConnection desktopConnection;
    private final DesktopSession session;

    public DesktopSessionMember(ConnectedUser connectedUser, SessionEventConnection eventConnection, RemoteDesktopConnection desktopConnection, DesktopSession session) {
        this.id = UUID.randomUUID().toString();
        this.connectedUser = connectedUser;
        this.eventConnection = eventConnection;
        this.desktopConnection = desktopConnection;
        this.session = session;
    }

    public String getId() {
        return id;
    }

    public ConnectedUser getConnectedUser() {
        return connectedUser;
    }

    public SessionEventConnection getEventConnection() {
        return eventConnection;
    }

    public RemoteDesktopConnection getDesktopConnection() {
        return desktopConnection;
    }

    public DesktopSession getSession() {
        return session;
    }

    public <T> void sendEvent(String type) {
        this.eventConnection.sendEvent(type);
    }

    public <T> void sendEvent(String type, T data) {
        this.eventConnection.sendEvent(type, data);
    }

    public void disconnect() {

        // Check for exceptions ?
        try {
            this.desktopConnection.disconnect();
            this.eventConnection.disconnect();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DesktopSessionMember that = (DesktopSessionMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
