package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;

import java.util.Date;

public class RemoteDesktopConnection {

    private final SocketClient client;
    private final ConnectionThread connectionThread;

    private Date lastSeenAt;
    private Date lastInteractionAt = new Date();
    private InstanceActivityType instanceActivityType;

    public RemoteDesktopConnection(SocketClient client, ConnectionThread connectionThread) {
        this.client = client;
        this.connectionThread = connectionThread;
    }

    public SocketClient getClient() {
        return client;
    }

    public ConnectionThread getConnectionThread() {
        return connectionThread;
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

    public InstanceActivityType getInstanceActivity() {
        return instanceActivityType;
    }

    public void resetInstanceActivity() {
        this.instanceActivityType = null;
    }

    public void setInstanceActivity(InstanceActivityType instanceActivityType) {
        if (this.instanceActivityType == null) {
            this.instanceActivityType = instanceActivityType;

        } else if (instanceActivityType.equals(InstanceActivityType.MOUSE) && this.instanceActivityType.equals(InstanceActivityType.KEYBOARD)) {
            this.instanceActivityType = InstanceActivityType.MOUSE_AND_KEYBOARD;

        } else if (instanceActivityType.equals(InstanceActivityType.KEYBOARD) && this.instanceActivityType.equals(InstanceActivityType.MOUSE)) {
            this.instanceActivityType = InstanceActivityType.MOUSE_AND_KEYBOARD;
        }
        this.updateLastInteractionAt();
    }

    public void updateLastInteractionAt() {
        this.lastInteractionAt = new Date();
    }

    public void disconnect() {
        this.client.disconnect();
    }
}
