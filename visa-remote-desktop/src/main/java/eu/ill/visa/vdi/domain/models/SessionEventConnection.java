package eu.ill.visa.vdi.domain.models;

public class SessionEventConnection {

    private final SocketClient client;

    public SessionEventConnection(SocketClient client) {
        this.client = client;
    }

    public <T> void sendEvent(String type) {
        this.client.sendEvent(type);
    }

    public <T> void sendEvent(String type, T data) {
        this.client.sendEvent(type, data);
    }

    public void disconnect() {
        this.client.disconnect();
    }

}
