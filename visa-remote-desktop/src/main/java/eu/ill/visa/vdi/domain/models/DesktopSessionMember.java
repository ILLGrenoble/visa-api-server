package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record DesktopSessionMember(String clientId, ConnectedUser connectedUser, RemoteDesktopConnection remoteDesktopConnection, DesktopSession session) {
    private static final Logger logger = LoggerFactory.getLogger(DesktopSessionMember.class);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DesktopSessionMember that = (DesktopSessionMember) o;
        return Objects.equals(clientId, that.clientId) && Objects.equals(connectedUser, that.connectedUser);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(clientId);
        result = 31 * result + Objects.hashCode(connectedUser);
        return result;
    }

    public String toString() {
        return String.format("User %s (role = %s) on Instance %s with protocol %s (clientId = %s}", connectedUser.getFullName(), connectedUser.getRole(), session.getInstanceId(), session.getProtocol(), clientId);
    }
}
