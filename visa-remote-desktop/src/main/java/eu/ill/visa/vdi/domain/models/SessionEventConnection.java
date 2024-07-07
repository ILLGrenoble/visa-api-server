package eu.ill.visa.vdi.domain.models;

public record SessionEventConnection(SocketClient client) {

    public <T> void sendEvent(String type) {
        this.sendEvent(type, null);
    }

    public <T> void sendEvent(String type, T data) {
        this.client.sendEvent(type, data);
    }

    public void disconnect() {
        this.client.disconnect();
    }
}
