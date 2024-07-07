package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DesktopSessionMember {
    private static final Logger logger = LoggerFactory.getLogger(DesktopSessionMember.class);

    private final String token;
    private final ConnectedUser connectedUser;
    private SessionEventConnection eventConnection;
    private final RemoteDesktopConnection remoteDesktopConnection;
    private final DesktopSession session;

    public DesktopSessionMember(final String token,
                                final ConnectedUser connectedUser,
                                final SessionEventConnection eventConnection,
                                final RemoteDesktopConnection remoteDesktopConnection,
                                final DesktopSession session) {
        this.token = token;
        this.connectedUser = connectedUser;
        this.eventConnection = eventConnection;
        this.remoteDesktopConnection = remoteDesktopConnection;
        this.session = session;
    }

    public String getToken() {
        return token;
    }

    public ConnectedUser getConnectedUser() {
        return connectedUser;
    }

    public void setEventConnection(SessionEventConnection eventConnection) {
        this.eventConnection = eventConnection;
    }

    public RemoteDesktopConnection getRemoteDesktopConnection() {
        return remoteDesktopConnection;
    }

    public DesktopSession getSession() {
        return session;
    }

    public <T> void sendEvent(String type) {
        this.sendEvent(type, null);
    }

    public <T> void sendEvent(String type, T data) {
        if (this.eventConnection != null) {
            this.eventConnection.sendEvent(type, data);

        } else {
            logger.warn("Attempting to send event to closed Event Channel for session member {}", this.token);
        }
    }

    public void disconnect() {
        try {
            this.remoteDesktopConnection.disconnect();
            this.eventConnection.disconnect();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public boolean isRole(final InstanceMemberRole role) {
        return this.connectedUser.isRole(role);
    }

    public boolean isEventConnectionOpen() {
        return this.eventConnection != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DesktopSessionMember that = (DesktopSessionMember) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token);
    }

    public String toString() {
        return String.format("User %s (role = %s) on Instance %s with protocol %s (token = %s}", connectedUser.getFullName(), connectedUser.getRole(), session.getInstanceId(), session.getProtocol(), token);
    }
}
