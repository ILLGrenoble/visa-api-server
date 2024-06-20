package eu.ill.visa.vdi.domain.models;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.UUID;

public class DesktopCandidate {

    private final SocketIOClient client;
    private final ConnectedUser user;
    private final Long instanceId;
    private final String token;

    public DesktopCandidate(final SocketIOClient client,
                            final ConnectedUser user,
                            final Long instanceId) {
        this.client = client;
        this.user = user;
        this.instanceId = instanceId;
        this.token = UUID.randomUUID().toString();
    }

    public SocketIOClient getClient() {
        return client;
    }

    public ConnectedUser getUser() {
        return user;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public String getToken() {
        return token;
    }

    public String getRoomId() {
        return this.instanceId.toString();
    }

    public String getConnectionId() {
        return this.client.getSessionId().toString();
    }

}
