package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DesktopSessionMember {
    private static final Logger logger = LoggerFactory.getLogger(DesktopSessionMember.class);

    private final String token;
    private final ConnectedUser connectedUser;
    private EventChannel eventChannel;
    private final RemoteDesktopConnection remoteDesktopConnection;
    private final DesktopSession session;

    public DesktopSessionMember(final String token,
                                final ConnectedUser connectedUser,
                                final EventChannel eventChannel,
                                final RemoteDesktopConnection remoteDesktopConnection,
                                final DesktopSession session) {
        this.token = token;
        this.connectedUser = connectedUser;
        this.eventChannel = eventChannel;
        this.remoteDesktopConnection = remoteDesktopConnection;
        this.session = session;
    }

    public String getToken() {
        return token;
    }

    public ConnectedUser getConnectedUser() {
        return connectedUser;
    }

    public void setEventChannel(EventChannel eventChannel) {
        this.eventChannel = eventChannel;
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
        if (this.eventChannel != null) {
            this.eventChannel.sendEvent(type, data);

        } else {
            logger.warn("Attempting to send event to closed Event Channel for session member {}", this);
        }
    }

    public void disconnect() {
        try {
            this.remoteDesktopConnection.disconnect();

        } catch (Exception e) {
            logger.error("Error while disconnecting the remote desktop for session member {}: {}", this, e.getMessage());
        }
    }

    public boolean isRole(final InstanceMemberRole role) {
        return this.connectedUser.isRole(role);
    }

    public boolean isEventChannelOpen() {
        return this.eventChannel != null;
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
