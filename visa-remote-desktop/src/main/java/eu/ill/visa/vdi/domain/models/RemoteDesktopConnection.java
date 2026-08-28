package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.domain.SampleStats;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class RemoteDesktopConnection {
    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopConnection.class);

    private final SocketClient client;
    private final ConnectionThread connectionThread;

    private Date lastInstanceUpdateTime;
    private Date lastInteractionAt = new Date();
    private InstanceActivityType instanceActivityType;

    private final RttSampler connectionRttSampler = new RttSampler();

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

    public Date getLastInstanceUpdateTime() {
        return lastInstanceUpdateTime;
    }

    public void setLastInstanceUpdateTime(Date lastInstanceUpdateTime) {
        this.lastInstanceUpdateTime = lastInstanceUpdateTime;
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

        this.setLastInteractionAt(new Date());
    }

    public void disconnect() {
        this.client.disconnect();
    }

    public void addClientRttSample(long clientRttSample) {
        this.connectionRttSampler.addClientRttSample(clientRttSample);
    }

    public void addRemoteDesktopRttMsSample(long remoteDesktopRttMsSample) {
        this.connectionRttSampler.addInstanceRttSample(remoteDesktopRttMsSample);
    }

    public Pair<SampleStats, SampleStats> calculateRttSampleStats() {
        return this.connectionRttSampler.calculateRttSampleStats();
    }

    public void resetRttSamples() {
        this.connectionRttSampler.reset();
    }

    public void setPingResponseHandler(final PingResponseHandler handler) {
        this.connectionThread.setPingResponseHandler(handler);
    }
}
