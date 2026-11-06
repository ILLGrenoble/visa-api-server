package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.common.utils.domain.IdleHandler;
import eu.ill.visa.common.utils.domain.Timer;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import io.smallrye.mutiny.subscription.Cancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static eu.ill.visa.common.utils.exceptions.ExceptionFormatter.formatException;

public record DesktopSessionMember(String clientId, ConnectedUser connectedUser, RemoteDesktopConnection remoteDesktopConnection, DesktopSession session, IdleHandler idleSessionHandler, Cancellable nopTimer) {

    private static final Logger logger = LoggerFactory.getLogger(DesktopSessionMember.class);
    private static final int IDLE_TIMEOUT_SECONDS = 60;
    private static final int NOP_INTERVAL_TIME_SECONDS = 5;

    public DesktopSessionMember(String clientId, ConnectedUser connectedUser, RemoteDesktopConnection remoteDesktopConnection, DesktopSession session, NopSender nopSender) {
        this(clientId,
            connectedUser,
            remoteDesktopConnection,
            session,
            new IdleHandler(IDLE_TIMEOUT_SECONDS),
            Timer.setInterval(() -> {
                nopSender.sendNop(remoteDesktopConnection.getClient());
            }, NOP_INTERVAL_TIME_SECONDS, TimeUnit.SECONDS)
        );
    }

    public void disconnect() {
        try {
            this.remoteDesktopConnection.disconnect();

        } catch (Exception e) {
            logger.error("Error while disconnecting the remote desktop for session member {}: {}", this, formatException(e));
        }
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
        return String.format("User %s (role = %s) on Instance %s with protocol %s (clientId = %s}", connectedUser.fullName(), connectedUser.role(), session.getInstance().id(), session.getProtocol(), clientId);
    }
}
