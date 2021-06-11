package eu.ill.visa.vdi.models;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.domain.User;

public class DesktopCandidate {

    private SocketIOClient client;
    private User user;
    private Long instanceId;

    public DesktopCandidate(SocketIOClient client, User user, Long instanceId) {
        this.client = client;
        this.user = user;
        this.instanceId = instanceId;
    }

    public SocketIOClient getClient() {
        return client;
    }

    public void setClient(SocketIOClient client) {
        this.client = client;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRoomId() {
        return this.instanceId.toString();
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
}
