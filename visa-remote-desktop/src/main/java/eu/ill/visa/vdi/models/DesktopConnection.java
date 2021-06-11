package eu.ill.visa.vdi.models;

import eu.ill.visa.vdi.concurrency.ConnectionThread;

import java.util.Date;

public class DesktopConnection {

    private final Long instanceId;
    private Date lastSeenAt;
    private Date lastInteractionAt = new Date() ;
    private final ConnectedUser connectedUser;
    private final ConnectionThread connectionThread;
    private final String roomId;
    private boolean isRoomLocked = false;

    public DesktopConnection(Long instanceId, Date lastSeenAt, final ConnectedUser connectedUser, final ConnectionThread connectionThread, String roomId) {
        this.instanceId = instanceId;
        this.lastSeenAt = lastSeenAt;
        this.connectedUser = connectedUser;
        this.connectionThread = connectionThread;
        this.roomId = roomId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public void updateLastInteractionAt() {
        this.lastInteractionAt = new Date();
    }

    public ConnectedUser getConnectedUser() {
        return connectedUser;
    }

    public ConnectionThread getConnectionThread() {
        return connectionThread;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public boolean isRoomLocked() {
        return isRoomLocked;
    }

    public void setRoomLocked(boolean roomLocked) {
        isRoomLocked = roomLocked;
    }
}
